package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.orm.ValuesMap
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Persisted
import com.rits.orm.utils.MapOfList
import com.rits.orm.Column
import com.rits.orm.UpdateInfo
import com.rits.orm.ColumnInfoOneToOneReverse

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseUpdatePlugin(mapperDao: MapperDao) extends DuringUpdate with PostUpdate {
	val typeRegistry = mapperDao.typeRegistry

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo) = entityMap.peek[Persisted, Any, T]
			if (parent != null) {
				parentColumnInfo match {
					case otor: ColumnInfoOneToOneReverse[_, T] =>
						val parentTpe = typeRegistry.typeOfObject(parent)
						otor.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parent)
					case _ => Nil
				}
			} else Nil
		}
	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val table = tpe.table
			// one-to-one-reverse
			table.oneToOneReverseColumnInfos.foreach { ci =>
				val fo = ci.columnToValue(o)
				val c = ci.column
				val fentity = typeRegistry.entityOfObject[Any, Any](fo)
				val ftpe = typeRegistry.typeOf(c.foreign.clz).asInstanceOf[Type[Nothing, Any]]
				val v = fo match {
					case p: Persisted =>
						entityMap.down(mockO, ci)
						mapperDao.updateInner(fentity, fo, entityMap)
						entityMap.up
				}
			}
		}
}