package com.googlecode.mapperdao.plugins
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneInsertPlugin(mapperDao: MapperDao) extends BeforeInsert {
	private val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val table = tpe.table
			var extraArgs = List[(Column, Any)]()
			// many-to-one
			table.manyToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOfObject[Any, Any](fo)
					val v = fo match {
						case null => null
						case p: Persisted =>
							entityMap.down(mockO, cis)
							val updated = mapperDao.updateInner(fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(fe, x, entityMap)
							entityMap.up
							inserted
					}
					val columns = cis.column.columns.filterNot(table.primaryKeyColumns.contains(_))
					if (!columns.isEmpty && columns.size != cis.column.columns.size) throw new IllegalStateException("only some of the primary keys were declared for %s, and those primary keys overlap manyToOne relationship declaration".format(tpe))
					extraArgs :::= columns zip typeRegistry.typeOf(fe).table.toListOfPrimaryKeyValues(v)
					v
				} else null
				modified(cis.column.alias) = v
			}
			extraArgs
		}
}