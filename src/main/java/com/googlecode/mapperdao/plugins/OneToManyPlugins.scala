package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyInsertPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PPC, PT, PC, T, V, FPC, F](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)] =
		{
			val tpe = entity.tpe
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = updateInfo

			if (parent != null) {
				val parentTpe = parentEntity.tpe
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case otm: OneToMany[_, _] =>
						val foreignKeyColumns = otm.foreignColumns.filterNot(tpe.table.primaryKeyColumns.contains(_))
						if (!foreignKeyColumns.isEmpty) {
							val parentTable = parentTpe.table
							val parentKeysAndValues = parent.asInstanceOf[Persisted].valuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
							val foreignKeys = parentKeysAndValues.map(_._2)
							if (foreignKeys.size != foreignKeyColumns.size) throw new IllegalArgumentException("mappings of one-to-many from " + parent + " to " + o + " is invalid. Number of FK columns doesn't match primary keys. columns: " + foreignKeyColumns + " , primary key values " + foreignKeys);
							foreignKeyColumns zip foreignKeys
						} else Nil
					case _ => Nil
				}
			} else Nil
		}

	override def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { cis =>
				val traversable = cis.columnToValue(o)
				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val cName = cis.column.alias
						traversable.foreach {
							modifiedTraversables(cName) = _
						}
						val handler = ee.oneToManyOnInsertMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnInsertOneToMany[T]]
						handler(InsertExternalOneToMany(updateConfig, o, traversable))

					case fe: Entity[Any, Any] =>
						val ftpe = fe.tpe
						val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
						if (traversable != null) {
							traversable.foreach { nested =>
								val newO = if (mapperDao.isPersisted(nested)) {
									val OneToMany(foreign: TypeRef[_, _], foreignColumns: List[Column]) = cis.column
									// update
									val keyArgs = ftpe.table.toListOfColumnAndValueTuples(ftpe.table.primaryKeys, nested)
									driver.doUpdateOneToManyRef(ftpe, foreignColumns zip newKeyValues, keyArgs)
									nested
								} else {
									// insert
									entityMap.down(mockO, cis, entity)
									val inserted = mapperDao.insertInner(updateConfig, fe, nested, entityMap)
									entityMap.up
									inserted
								}
								val cName = cis.column.alias
								modifiedTraversables(cName) = newO
							}
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
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { ci =>
				val c = ci.column
				val otmL = if (selectConfig.skip(ci)) {
					Nil
				} else
					c.foreign.entity match {
						case ee: ExternalEntity[Any] =>
							val table = tpe.table
							val ids = table.primaryKeys.map { pk =>
								om(pk.column.columnName)
							}
							ee.oneToManyOnSelectMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, Any]])(SelectExternalOneToMany(selectConfig, ids))
						case fe: Entity[_, _] =>
							val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
							val where = c.foreignColumns.zip(ids)
							val ftpe = fe.tpe
							val fom = driver.doSelect(ftpe, where)
							entities.down(tpe, ci, om)
							val v = mapperDao.toEntities(fom, fe, selectConfig, entities)
							entities.up
							v
					}
				mods(c.foreign.alias) = otmL
			}
		}

	override def updateMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= entity.tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends PostUpdate {

	def after[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]) =
		{
			val tpe = entity.tpe
			// update one-to-many
			val table = tpe.table

			table.oneToManyColumnInfos.filterNot(updateConfig.skip.contains(_)).foreach { ci =>
				val oneToMany = ci.column
				val t = ci.columnToValue(o)
				// we'll get the 2 traversables and update the database
				// based on their differences
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](oneToMany.foreign.alias)

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)

				ci.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>

						val handler = ee.oneToManyOnUpdateMap(ci.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnUpdateOneToMany[T]]
						handler(UpdateExternalOneToMany(updateConfig, o, added, intersection, removed))
						t.foreach { newItem =>
							modified(oneToMany.alias) = newItem
						}
					case fe: Entity[Any, Any] =>

						// update the removed ones
						removed.foreach { item =>
							entityMap.down(mockO, ci, entity)
							mapperDao.deleteInner(updateConfig.deleteConfig, fe, item, entityMap)
							entityMap.up
						}

						// update those that remained in the updated traversable
						intersection.foreach { item =>
							entityMap.down(mockO, ci, entity)
							val newItem = mapperDao.updateInner(updateConfig, fe, item, entityMap)
							entityMap.up
							item.asInstanceOf[Persisted].discarded = true
							modified(oneToMany.alias) = newItem
						}
						// find the added ones
						added.foreach { item =>
							entityMap.down(mockO, ci, entity)
							val newItem: Any = mapperDao.insertInner(updateConfig, fe, item, entityMap);
							entityMap.up
							modified(oneToMany.alias) = newItem
						}
				}
			}
		}
}

class OneToManyDeletePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeDelete {

	override def idColumnValueContribution[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)] = {
		val UpdateInfo(parentO, ci, parentEntity) = entityMap.peek[Any, Any, Traversable[T], Any, T]
		ci match {
			case oneToMany: ColumnInfoTraversableOneToMany[_, _, T] =>
				val parentTpe = parentEntity.tpe
				oneToMany.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parentO)
			case _ => Nil
		}
	}

	override def before[PC, T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) =
		if (deleteConfig.propagate) {
			val tpe = entity.tpe
			tpe.table.oneToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { cis =>

				// execute before-delete-relationship events
				events.executeBeforeDeleteRelationshipEvents(tpe, cis, o)

				val fOTraversable = cis.columnToValue(o)

				cis.column.foreign.entity match {
					case ee: ExternalEntity[Any] =>
						val handler = ee.oneToManyOnDeleteMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[T, _, Any]])
							.asInstanceOf[ee.OnDeleteOneToMany[T]]
						handler(DeleteExternalOneToMany(deleteConfig, o, fOTraversable))

					case fe: Entity[Any, Any] =>
						if (fOTraversable != null) fOTraversable.foreach { fO =>
							val fOPersisted = fO.asInstanceOf[Persisted]
							if (!fOPersisted.mock) {
								mapperDao.delete(deleteConfig, fe, fOPersisted)
							}
						}
				}
				// execute after-delete-relationship events
				events.executeAfterDeleteRelationshipEvents(tpe, cis, o)
			}
		}
}