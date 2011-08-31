package com.rits.orm.plugins
import com.rits.orm.MapperDao
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Type
import com.rits.orm.Persisted
import com.rits.orm.Column
import com.rits.orm.UpdateInfo

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneInsertPlugin(mapperDao: MapperDao) extends BeforeInsert {
	private val typeRegistry = mapperDao.typeRegistry

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], updateInfo: UpdateInfo[Persisted, V, T]): List[(Column, Any)] =
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
					extraArgs :::= cis.column.columns zip typeRegistry.typeOf(fe).table.toListOfPrimaryKeyValues(v)
					v
				} else null
				modified(cis.column.alias) = v
			}
			extraArgs
		}
}