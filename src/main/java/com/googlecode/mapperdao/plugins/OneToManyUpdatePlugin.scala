package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(mapperDao: MapperDao) extends PostUpdate {
	val typeRegistry = mapperDao.typeRegistry

	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			// update one-to-many
			val table = tpe.table

			table.oneToManyColumnInfos.foreach { ci =>
				val t: Traversable[Any] = ci.columnToValue(o)

				val oneToMany = ci.column
				// we'll get the 2 traversables and update the database
				// based on their differences
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](oneToMany.foreign.alias)

				// update those that remained in the updated traversable
				val intersection = newValues.intersect(oldValues)
				intersection.foreach { item =>
					val fe = typeRegistry.entityOfObject[Any, Any](item)
					entityMap.down(mockO, ci)
					val newItem = mapperDao.updateInner(fe, item, entityMap)
					entityMap.up
					item.asInstanceOf[Persisted].discarded = true
					modified(oneToMany.alias) = newItem
					//addToMap(oneToMany.alias, newItem, modifiedTraversables)
				}
				// find the added ones
				val diff = newValues.diff(oldValues)
				diff.foreach { item =>
					//val keysAndValues = table.primaryKeys.map(_.column) zip table.primaryKeys.map(c => modified(c.columnName))
					val fe = typeRegistry.entityOfObject(item)
					entityMap.down(mockO, ci)
					val newItem: Any = mapperDao.insertInner(fe, item, entityMap);
					entityMap.up
					modified(oneToMany.alias) = newItem
					//addToMap(oneToMany.alias, newItem, modifiedTraversables)
				}

				// find the removed ones
				val odiff = oldValues.diff(newValues)
				odiff.foreach { item =>
					val fe = typeRegistry.entityOfObject[Any, Any](item)
					mapperDao.delete(fe, item)
				}
			}
		}
}