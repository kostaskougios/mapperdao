package com.rits.orm.plugins
import com.rits.orm.Column
import com.rits.orm.Type
import com.rits.orm.ColumnBase
import com.rits.orm.ValuesMap
import com.rits.orm.MapperDao
import com.googlecode.mapperdao.utils.MapOfList
import com.rits.orm.UpdateEntityMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate {

	private val typeRegistry = mapperDao.typeRegistry

	private def onlyChanged(column: ColumnBase, newValuesMap: ValuesMap, oldValuesMap: ValuesMap) = newValuesMap(column.alias) != oldValuesMap(column.alias)

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val table = tpe.table
			val manyToOneChanged = table.manyToOneColumns.filter(onlyChanged(_, newValuesMap, oldValuesMap))
			val mtoArgsV = manyToOneChanged.map(mto => (mto.foreign.clz, newValuesMap[Any](mto.alias))).map { t =>
				typeRegistry.typeOf(t._1).table.toListOfPrimaryKeyValues(t._2)
			}.flatten
			new DuringUpdateResults(manyToOneChanged.map(_.columns).flatten zip mtoArgsV, Nil)
		}

}