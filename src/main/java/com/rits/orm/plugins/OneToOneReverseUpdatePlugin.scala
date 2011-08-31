package com.rits.orm.plugins

import com.rits.orm.Type
import com.rits.orm.MapperDao
import com.rits.orm.ValuesMap
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Persisted
import com.rits.orm.utils.MapOfList

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneReverseUpdatePlugin(mapperDao: MapperDao) extends PostUpdate {
	val typeRegistry = mapperDao.typeRegistry

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