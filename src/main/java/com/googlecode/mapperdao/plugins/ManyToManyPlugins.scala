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
/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends PostInsert {

	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = entity.tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					val nestedEntity = cis.column.foreign.entity.asInstanceOf[Entity[Any, Any]]
					val nestedTpe = nestedEntity.tpe
					traversable.foreach { nested =>
						val newO = if (mapperDao.isPersisted(nested)) {
							nested
						} else nestedEntity match {
							case ee: ExternalEntity[_, _] =>
								nested
							case _ =>
								entityMap.down(mockO, cis, entity)
								val inserted = mapperDao.insertInner(updateConfig, nestedEntity, nested, entityMap)
								entityMap.up
								inserted
						}
						val rightKeyValues = nestedTpe.table.toListOfPrimaryKeyAndValueTuples(newO)
						val newKeyColumnAndValues = table.primaryKeys.map(_.column) zip newKeyValues
						driver.doInsertManyToMany(nestedTpe, cis.column, newKeyColumnAndValues, rightKeyValues)
						val cName = cis.column.alias
						modifiedTraversables(cName) = newO
					}
				}
			}
		}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToMany[Any, Any]]
				val fe = c.foreign.entity
				val ftpe = fe.tpe
				val mtmR = if (selectConfig.skip(ci)) {
					Nil
				} else {
					val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
					val keys = c.linkTable.left zip ids
					val fom = driver.doSelectManyToMany(tpe, ftpe, c, keys)
					entities.down(tpe, ci, om)
					val mtmR = mapperDao.toEntities(fom, fe, selectConfig, entities)
					entities.up
					mtmR
				}
				mods(c.foreign.alias) = mtmR
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= entity.tpe.table.manyToManyColumns.map(c => (c.alias -> List()))
	}
}

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
			table.manyToManyColumnInfos.foreach { ci =>
				val t = ci.columnToValue(o)
				val manyToMany = ci.column
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](manyToMany.foreign.alias)

				val pkArgs = manyToMany.linkTable.left zip oldValuesMap.toListOfColumnValue(table.primaryKeys)

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)

				val fe = manyToMany.foreign.entity.asInstanceOf[Entity[Any, Any]]
				val ftpe = fe.tpe
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
					val fPKArgs = manyToMany.linkTable.right zip ftpe.table.toListOfPrimaryKeyValues(newItem)
					driver.doInsertManyToMany(tpe, manyToMany, pkArgs, fPKArgs)
					modified(manyToMany.alias) = newItem
				}
			}
		}
}

class ManyToManyDeletePlugin(driver: Driver, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = Nil

	override def before[PC, T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) = if (deleteConfig.propagate) {
		val tpe = entity.tpe
		tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>
			// execute before-delete-relationship events
			events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)

			driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

			// execute after-delete-relationship events
			events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
		}
	}
}