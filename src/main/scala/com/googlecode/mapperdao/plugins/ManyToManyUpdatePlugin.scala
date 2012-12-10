package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyUpdatePlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends PostUpdate {

	override def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: MapOfList[String, Any]) =
		{
			val entity = node.entity
			val newVM = node.newVM
			val oldVM = node.oldVM
			val tpe = entity.tpe
			val table = tpe.table
			// update many-to-many
			node.manyToMany
				.filterNot(t => updateConfig.skip.contains(t._1))
				.foreach {
					case (ci, childNode) =>
						val newValues = newVM.valueOf(ci)
						val oldValues = oldVM.valueOf(ci).asInstanceOf[Traversable[DeclaredIds[Any]]]

						val manyToMany = ci.column
						val pkLeft = oldVM.toListOfColumnValue(table.primaryKeys)
						val pkArgs = manyToMany.linkTable.left zip pkLeft

						val fentity = ci.column.foreign.entity.asInstanceOf[Entity[Any, DeclaredIds[Any], Any]]

						val (added, intersection, removed) = TraversableSeparation.separate(fentity, oldValues, newValues)

						val fe = manyToMany.foreign.entity.asInstanceOf[Entity[Any, DeclaredIds[Any], Any]]
						val ftpe = fe.tpe

						manyToMany.foreign.entity match {
							case ee: ExternalEntity[Any, Any] =>
								val handler = ee.manyToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableManyToMany[_, _, _, Any]])
									.asInstanceOf[ee.OnUpdateManyToMany[T]]
								// delete the removed ones
								removed.foreach { p =>
									val ftable = ftpe.table
									val rightKeyValues = handler(UpdateExternalManyToMany(updateConfig, UpdateExternalManyToMany.Operation.Remove, newVM, p))

									val fPkArgs = manyToMany.linkTable.right zip rightKeyValues.values
									driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
								}
								// update those that remained in the updated traversable
								intersection.foreach {
									case (oldV, newV) =>
										modified(manyToMany.alias) = newV
								}
								// update the added ones
								added.foreach { p =>
									val fPKArgs = handler(UpdateExternalManyToMany(updateConfig, UpdateExternalManyToMany.Operation.Add, newVM, p))
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
								}

								// update those that remained in the updated traversable
								intersection.foreach {
									case (oldV, newV) =>
										entityMap.down(mockO, ci, entity)
										mapperDao.updateInner(updateConfig, childNode, entityMap)
										entityMap.up
										modified(manyToMany.alias) = oldV
								}

								// update the added ones
								added.foreach { item =>
									val newItem = item match {
										case p: Persisted => p
										case n =>
											entityMap.down(mockO, ci, entity)
											val inserted = mapperDao.insertInner(updateConfig, childNode, entityMap)
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
