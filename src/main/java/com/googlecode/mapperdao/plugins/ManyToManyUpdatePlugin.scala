package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.InsertExternalManyToMany
import com.googlecode.mapperdao.UpdateExternalManyToMany
import com.googlecode.mapperdao.SelectExternalManyToMany
import com.googlecode.mapperdao.DeleteExternalManyToMany

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyUpdatePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends PostUpdate {

	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// update many-to-many
			table.manyToManyColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val t = ci.columnToValue(o)
				val manyToMany = ci.column
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](manyToMany.foreign.alias)

				val pkLeft = oldValuesMap.toListOfColumnValue(table.primaryKeys)
				val pkArgs = manyToMany.linkTable.left zip pkLeft

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)

				val fe = manyToMany.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val ftpe = fe.tpe

				manyToMany.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val handler = ee.manyToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, Any]])
							.asInstanceOf[ee.OnUpdateManyToMany[T]]
						// delete the removed ones
						removed.foreach { p =>
							val ftable = ftpe.table
							val rightKeyValues = handler(UpdateExternalManyToMany(updateConfig, UpdateExternalManyToMany.Operation.Remove, o, p))

							val fPkArgs = manyToMany.linkTable.right zip rightKeyValues.values
							driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
						}
						// update those that remained in the updated traversable
						intersection.foreach { p =>
							modified(manyToMany.alias) = p
						}
						// update the added ones
						added.foreach { p =>
							val fPKArgs = handler(UpdateExternalManyToMany(updateConfig, UpdateExternalManyToMany.Operation.Add, o, p))
							driver.doInsertManyToMany(tpe, manyToMany, pkLeft, fPKArgs.values)
							modified(manyToMany.alias) = p
						}

					case _ =>
						// delete the removed ones
						removed.foreach {
							case p: Persisted =>
								val ftable = ftpe.table
								val fPkArgs = manyToMany.linkTable.right zip ftable.toListOfPrimaryKeyValues(p)
								driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
								p.discarded = true
						}

						// update those that remained in the updated traversable
						intersection.foreach { item =>
							val newItem = item match {
								case p: Persisted =>
									entityMap.down(mockO, ci, entity)
									mapperDao.updateInner(updateConfig, fe, item, entityMap)
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
									entityMap.down(mockO, ci, entity)
									val inserted = mapperDao.insertInner[Any, Any](updateConfig, fe, n, entityMap)
									entityMap.up
									inserted
							}
							val fPKArgs = ftpe.table.toListOfPrimaryKeyValues(newItem)
							driver.doInsertManyToMany(tpe, manyToMany, pkLeft, fPKArgs)
							modified(manyToMany.alias) = newItem
						}
				}
			}
		}
}
