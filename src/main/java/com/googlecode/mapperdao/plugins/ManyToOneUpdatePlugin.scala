package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.Persisted

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate {

	private val typeRegistry = mapperDao.typeRegistry

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val table = tpe.table

			table.manyToOneColumnInfos.foreach { ci =>
				val v = ci.columnToValue(o)
				val newV = v match {
					case null => null //throw new NullPointerException("unexpected null for primary entity on ManyToOne mapping, for entity %s.".format(o))
					case p: Persisted =>
						val fEntity = typeRegistry.entityOfObject[Any, Any](v)
						entityMap.down(o, ci)
						val newV = mapperDao.updateInner(fEntity, v, entityMap)
						entityMap.up
						newV
					case _ =>
						val fEntity = typeRegistry.entityOfObject[Any, Any](v)
						entityMap.down(o, ci)
						val newV = mapperDao.insertInner(fEntity, v, entityMap)
						entityMap.up
						newV
				}
				modified(ci.column.alias) = newV
			}

			val manyToOneChanged = table.manyToOneColumns.filter(Equality.onlyChanged(_, newValuesMap, oldValuesMap))
			val mtoArgsV = manyToOneChanged.map(mto => (mto.foreign.clz, newValuesMap[Any](mto.alias))).map { t =>
				typeRegistry.typeOf(t._1).table.toListOfPrimaryKeyValues(t._2)
			}.flatten
			val cv = (manyToOneChanged.map(_.columns).flatten zip mtoArgsV) filterNot (cav => table.primaryKeyColumns.contains(cav._1))
			new DuringUpdateResults(cv, Nil)
		}

}