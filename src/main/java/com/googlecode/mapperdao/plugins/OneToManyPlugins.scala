package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.events.Events

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyInsertPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo) = updateInfo
			if (parent != null) {
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case otm: OneToMany[_] =>
						val foreignKeyColumns = otm.foreignColumns.filterNot(tpe.table.primaryKeyColumns.contains(_))
						if (!foreignKeyColumns.isEmpty) {
							val parentEntity = typeRegistry.entityOfObject[Any, Any](parent)
							val parentTpe = typeRegistry.typeOf(parentEntity)
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

	override def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { cis =>
				val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOfObject[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (mapperDao.isPersisted(nested)) {
							val OneToMany(foreign: TypeRef[_], foreignColumns: List[Column]) = cis.column
							// update
							val keyArgs = nestedTpe.table.toListOfColumnAndValueTuples(nestedTpe.table.primaryKeys, nested)
							driver.doUpdateOneToManyRef(nestedTpe, foreignColumns zip newKeyValues, keyArgs)
							nested
						} else {
							// insert
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(nestedEntity, nested, entityMap)
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

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManySelectPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// one to many
			table.oneToManyColumnInfos.foreach { ci =>
				val c = ci.column
				val otmL = if (selectConfig.skip(ci)) {
					Nil
				} else {
					val ftpe = typeRegistry.typeOf(c.foreign.clz)
					val ids = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
					val where = c.foreignColumns.zip(ids)
					val fom = driver.doSelect(ftpe, where)
					entities.down(tpe, ci, om)
					val v = mapperDao.toEntities(fom, ftpe, selectConfig, entities)
					entities.up
					v
				}
				mods(c.foreign.alias) = otmL
			}
		}

	override def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends PostUpdate {

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

				val (added, intersection, removed) = TraversableSeparation.separate(oldValues, newValues)

				// update the removed ones
				removed.foreach { item =>
					val fe = typeRegistry.entityOfObject[Any, Any](item)
					entityMap.down(mockO, ci)
					mapperDao.deleteInner(mapperDao.defaultDeleteConfig, fe, item, entityMap)
					entityMap.up
				}

				// update those that remained in the updated traversable
				intersection.foreach { item =>
					val fe = typeRegistry.entityOfObject[Any, Any](item)
					entityMap.down(mockO, ci)
					val newItem = mapperDao.updateInner(fe, item, entityMap)
					entityMap.up
					item.asInstanceOf[Persisted].discarded = true
					modified(oneToMany.alias) = newItem
				}
				// find the added ones
				added.foreach { item =>
					val fe = typeRegistry.entityOfObject(item)
					entityMap.down(mockO, ci)
					val newItem: Any = mapperDao.insertInner(fe, item, entityMap);
					entityMap.up
					modified(oneToMany.alias) = newItem
				}
			}
		}
}

class OneToManyDeletePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends BeforeDelete {
	override def before[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap) = if (deleteConfig.propagate) {
		tpe.table.oneToManyColumnInfos.filterNot(deleteConfig.skip(_)).foreach { ci =>

			// execute before-delete-relationship events
			events.executeBeforeDeleteRelationshipEvents(tpe, ci, o)

			val fOTraversable = ci.columnToValue(o)
			if (fOTraversable != null) fOTraversable.foreach { fO =>
				val fOPersisted = fO.asInstanceOf[Persisted]
				if (!fOPersisted.mock) {
					val fEntity = typeRegistry.entityOfObject[Any, Any](fOPersisted)
					mapperDao.delete(deleteConfig, fEntity, fOPersisted)
				}
			}

			// execute after-delete-relationship events
			events.executeAfterDeleteRelationshipEvents(tpe, ci, o)
		}
	}
}