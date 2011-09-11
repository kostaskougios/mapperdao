package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation

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

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)
				// delete the removed ones
				removed.foreach(_ match {
					case p: Persisted =>
						val ftpe = typeRegistry.typeOfObject[Any, Any](p)
						val ftable = ftpe.table
						val fPkArgs = manyToMany.linkTable.right zip ftable.toListOfPrimaryKeyValues(p)
						driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
						p.discarded = true
				})

				// update those that remained in the updated traversable
				intersection.foreach { item =>
					val newItem = item match {
						case p: Persisted =>
							val fe = typeRegistry.entityOfObject[Any, Any](item)
							entityMap.down(mockO, ci)
							mapperDao.updateInner(fe, item, entityMap)
							entityMap.up
							p.discarded = true
							p
						case _ =>
							throw new IllegalStateException("Object not persisted but still exists in intersection of old and new collections. Please use the persisted entity when modifying the collection. The not persisted object is %s.".format(item))
					}
					modified(manyToMany.alias) = newItem
				}

				// update the added ones
				added.foreach { item =>
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
			}
		}
}