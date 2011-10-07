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

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToManyInsertPlugin(mapperDao: MapperDao) extends PostInsert {
	val typeRegistry = mapperDao.typeRegistry
	val driver = mapperDao.driver

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOfObject[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (mapperDao.isPersisted(nested)) {
							nested
						} else {
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(nestedEntity, nested, entityMap)
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
class ManyToManySelectPlugin(mapperDao: MapperDao) extends BeforeSelect with SelectMock {
	private val typeRegistry = mapperDao.typeRegistry
	private val driver = mapperDao.driver

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to many
			table.manyToManyColumnInfos.foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToMany[Any]]
				val mtmR = if (selectConfig.skip(ci)) {
					Nil
				} else {
					val ftpe = typeRegistry.typeOf(c.foreign.clz)
					val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
					val fom = driver.doSelectManyToMany(tpe, ftpe, c, c.linkTable.left zip ids)
					entities.down(tpe, ci, om)
					val mtmR = mapperDao.toEntities(fom, ftpe, selectConfig, entities)
					entities.up
					mtmR
				}
				mods(c.foreign.alias) = mtmR
			}
		}

	override def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= tpe.table.manyToManyColumns.map(c => (c.alias -> List()))
	}
}

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

class ManyToManyDeletePlugin(mapperDao: MapperDao) extends BeforeDelete {
	val driver = mapperDao.driver
	override def before[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)]) = if (deleteConfig.propagate) {
		tpe.table.manyToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>
			// execute before-delete-relationship events
			events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)

			driver.doDeleteAllManyToManyRef(tpe, ci.column, keyValues.map(_._2))

			// execute after-delete-relationship events
			events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
		}
	}
}