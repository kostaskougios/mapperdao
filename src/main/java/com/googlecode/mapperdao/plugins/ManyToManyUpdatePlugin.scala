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
class ManyToManyUpdatePlugin(mapperDao: MapperDao) extends PostUpdate {
	val driver = mapperDao.driver
	val typeRegistry = mapperDao.typeRegistry

	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val table = tpe.table
			// update many-to-many
			table.manyToManyColumnInfos.foreach { ci =>
				val t = ci.columnToValue(o)
				val manyToMany = ci.column
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](manyToMany.foreign.alias)

				val pkArgs = manyToMany.linkTable.left zip oldValuesMap.toListOfColumnValue(table.primaryKeys)

				// update those that remained in the updated traversable
				val intersection = newValues.intersect(oldValues)
				intersection.foreach { item =>
					val newItem = item match {
						case p: Persisted if (!p.mock) =>
							val fe = typeRegistry.entityOfObject[Any, Any](item)
							entityMap.down(mockO, ci)
							mapperDao.updateInner(fe, item, entityMap)
							entityMap.up
							p.discarded = true
						case _ => item
					}
					modified(manyToMany.alias) = newItem
				}

				// find the added ones
				val diff = newValues.diff(oldValues)
				diff.foreach { item =>
					val newItem = item match {
						case p: Persisted => p
						case n =>
							entityMap.down(mockO, ci)
							val inserted = mapperDao.insertInner[Any, Any](typeRegistry.entityOfObject(n), n, entityMap)
							entityMap.up
							inserted
					}
					val ftpe = typeRegistry.typeOfObject(newItem)
					val fPKArgs = manyToMany.linkTable.right zip ftpe.table.toListOfPrimaryKeyValues(newItem)
					driver.doInsertManyToMany(tpe, manyToMany, pkArgs, fPKArgs)
					modified(manyToMany.alias) = newItem
				}
				// find the removed ones
				val odiff = oldValues.diff(newValues)
				odiff.foreach(_ match {
					case p: Persisted =>
						val ftpe = typeRegistry.typeOfObject[Any, Any](p)
						val ftable = ftpe.table
						val fPkArgs = manyToMany.linkTable.right zip ftable.toListOfPrimaryKeyValues(p)
						driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
						p.discarded = true
				})
			}
		}
}