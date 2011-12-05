package com.googlecode.mapperdao

import com.googlecode.mapperdao.drivers.Driver
import scala.collection.mutable.HashMap
import com.googlecode.mapperdao.exceptions._
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.plugins._
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.plugins.SelectMock
import utils.LowerCaseMutableMap
import com.googlecode.mapperdao.events.Events

trait MapperDao {

	// insert
	def insert[PC, T](entity: Entity[PC, T], o: T): T with PC = insert(defaultUpdateConfig, entity, o)
	def insert[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T): T with PC

	// update
	def update[PC, T](entity: Entity[PC, T], o: T with PC): T with PC = update(defaultUpdateConfig, entity, o)
	def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC): T with PC

	def update[PC, T](entity: Entity[PC, T], o: T with PC, newO: T): T with PC = update(defaultUpdateConfig, entity, o, newO)
	def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC, newO: T): T with PC

	// select
	/**
	 * select an entity by it's ID
	 *
	 * @param clz		Class[T], classOf[Entity]
	 * @param id		the id
	 * @return			Option[T] or None
	 */
	def select[PC, T](entity: Entity[PC, T], id: Any): Option[T with PC] = select(entity, List(id))
	def select[PC, T](entity: Entity[PC, T], id1: Any, id2: Any): Option[T with PC] = select(entity, List(id1, id2))
	def select[PC, T](entity: Entity[PC, T], id1: Any, id2: Any, id3: Any): Option[T with PC] = select(entity, List(id1, id2, id3))

	def select[PC, T](entity: Entity[PC, T], ids: List[Any]): Option[T with PC] = select(defaultSelectConfig, entity, ids)

	/**
	 * select an entity but load only part of the entity's graph. SelectConfig contains configuration regarding which relationships
	 * won't be loaded, i.e.
	 *
	 * SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
	 */
	def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], id: Any): Option[T with PC] = select(selectConfig, entity, List(id))

	def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC]

	// default configurations
	val defaultSelectConfig = SelectConfig()
	val defaultDeleteConfig = DeleteConfig()
	val defaultUpdateConfig = UpdateConfig(defaultDeleteConfig)

	// delete

	/**
	 * deletes an entity from the database. By default, related entities won't be deleted, please use
	 * delete(deleteConfig, entity, o) to fine tune the operation
	 */
	def delete[PC, T](entity: Entity[PC, T], o: T with PC): T = delete(defaultDeleteConfig, entity, o)
	def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T

	/**
	 * ===================================================================================
	 * ID helper methods
	 * ===================================================================================
	 */
	/**
	 * retrieve the id of an entity
	 */
	def intIdOf(o: AnyRef): Int = o match {
		case iid: IntId => iid.id
	}

	/**
	 * retrieve the id of an entity
	 */
	def longIdOf(o: AnyRef): Long = o match {
		case iid: LongId => iid.id
	}

	// used internally
	//private[mapperdao] def toEntities[PC, T](lm: List[JdbcMap], entity: Entity[PC, T], selectConfig: SelectConfig, entities: EntityMap): List[T with PC]
}

/**
 * @author kostantinos.kougios
 *
 * 13 Jul 2011
 */
protected final class MapperDaoImpl(val driver: Driver, events: Events) extends MapperDao {
	private val typeRegistry = driver.typeRegistry
	private val typeManager = driver.jdbc.typeManager

	private val postUpdatePlugins = List[PostUpdate](new OneToOneReverseUpdatePlugin(typeRegistry, typeManager, driver, this), new OneToManyUpdatePlugin(typeRegistry, this), new ManyToManyUpdatePlugin(typeRegistry, driver, this))
	private val duringUpdatePlugins = List[DuringUpdate](new ManyToOneUpdatePlugin(typeRegistry, this), new OneToOneReverseUpdatePlugin(typeRegistry, typeManager, driver, this), new OneToOneUpdatePlugin(typeRegistry, this))
	private val beforeInsertPlugins = List[BeforeInsert](new ManyToOneInsertPlugin(typeRegistry, this), new OneToManyInsertPlugin(typeRegistry, driver, this), new OneToOneReverseInsertPlugin(typeRegistry, this), new OneToOneInsertPlugin(typeRegistry, this))
	private val postInsertPlugins = List[PostInsert](new OneToOneReverseInsertPlugin(typeRegistry, this), new OneToManyInsertPlugin(typeRegistry, driver, this), new ManyToManyInsertPlugin(typeRegistry, driver, this))
	private val selectBeforePlugins: List[BeforeSelect] = List(new ManyToOneSelectPlugin(typeRegistry, this), new OneToManySelectPlugin(typeRegistry, driver, this), new OneToOneReverseSelectPlugin(typeRegistry, driver, this), new OneToOneSelectPlugin(typeRegistry, driver, this), new ManyToManySelectPlugin(typeRegistry, driver, this))
	private val mockPlugins: List[SelectMock] = List(new OneToManySelectPlugin(typeRegistry, driver, this), new ManyToManySelectPlugin(typeRegistry, driver, this), new ManyToOneSelectPlugin(typeRegistry, this), new OneToOneSelectPlugin(typeRegistry, driver, this))
	private val beforeDeletePlugins: List[BeforeDelete] = List(new ManyToManyDeletePlugin(driver, this), new OneToManyDeletePlugin(typeRegistry, this), new OneToOneReverseDeletePlugin(typeRegistry, driver, this))
	/**
	 * ===================================================================================
	 * Utility methods
	 * ===================================================================================
	 */

	private[mapperdao] def isPersisted(o: Any): Boolean = o.isInstanceOf[Persisted]

	/**
	 * ===================================================================================
	 * CRUD OPERATIONS
	 * ===================================================================================
	 */

	private[mapperdao] def insertInner[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, entityMap: UpdateEntityMap): T with PC with Persisted =
		// if a mock exists in the entity map or already persisted, then return
		// the existing mock/persisted object
		entityMap.get[PC, T](o).getOrElse {

			if (isPersisted(o)) throw new IllegalArgumentException("can't insert an object that is already persisted: " + o);

			val tpe = entity.tpe
			val table = tpe.table

			val modified = ValuesMap.fromEntity(typeManager, tpe, o).toLowerCaseMutableMap
			val modifiedTraversables = new MapOfList[String, Any](MapOfList.stringToLowerCaseModifier)

			val updateInfo @ UpdateInfo(parent, parentColumnInfo, parentEntity) = entityMap.peek[Any, Any, Any, PC, T]

			// create a mock
			var mockO = createMock(entity, modified ++ modifiedTraversables)
			entityMap.put(o, mockO)

			val extraArgs = beforeInsertPlugins.map { plugin =>
				plugin.before(updateConfig, entity, o, mockO, entityMap, modified, updateInfo)
			}.flatten.distinct

			// arguments
			val args = table.toListOfColumnAndValueTuples(table.simpleTypeNotAutoGeneratedColumns, o) ::: extraArgs

			// insert entity
			if (!args.isEmpty || !table.simpleTypeAutoGeneratedColumns.isEmpty) {
				events.executeBeforeInsertEvents(tpe, args)
				val ur = driver.doInsert(tpe, args)
				events.executeAfterInsertEvents(tpe, args)

				table.simpleTypeAutoGeneratedColumns.foreach { c =>
					modified(c.columnName) = driver.getAutoGenerated(ur, c)
				}
			}

			// create a more up-to-date mock
			mockO = createMock(entity, modified ++ modifiedTraversables)
			entityMap.put(o, mockO)

			postInsertPlugins.foreach { plugin =>
				plugin.after(updateConfig, entity, o, mockO, entityMap, modified, modifiedTraversables)
			}

			val finalMods = modified ++ modifiedTraversables
			val newE = tpe.constructor(ValuesMap.fromMutableMap(typeManager, finalMods))
			// re-put the actual
			entityMap.put(o, newE)
			newE
		}

	/**
	 * insert an entity into the database
	 */
	def insert[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T): T with PC =
		{
			val entityMap = new UpdateEntityMap
			try {
				val v = insertInner(updateConfig, entity, o, entityMap)
				entityMap.done
				v
			} catch {
				case e =>
					val stack = entityMap.toErrorStr
					throw new PersistException("An error occured during insert of entity %s with value %s.\nUpdate stack (top is more recent):\n%s".format(entity, o, stack), e)
			}
		}

	/**
	 * update an entity
	 */
	private def updateInner[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap): T with PC with Persisted =
		{
			val tpe = entity.tpe
			def changed(column: ColumnBase) = newValuesMap.valueOf(column.alias) != oldValuesMap.valueOf(column.alias)
			val table = tpe.table
			val modified = new LowerCaseMutableMap[Any](oldValuesMap.toMutableMap ++ newValuesMap.toMutableMap)
			val modifiedTraversables = new MapOfList[String, Any](MapOfList.stringToLowerCaseModifier)

			// store a mock in the entity map so that we don't process the same instance twice
			var mockO = createMock(entity, modified ++ modifiedTraversables)
			entityMap.put(o, mockO)

			// first, lets update the simple columns that changed

			// run all DuringUpdate plugins
			val pluginDUR = duringUpdatePlugins.map { plugin =>
				plugin.during(updateConfig, entity, o, oldValuesMap, newValuesMap, entityMap, modified, modifiedTraversables)
			}.reduceLeft { (dur1, dur2) =>
				new DuringUpdateResults(dur1.values ::: dur2.values, dur1.keys ::: dur2.keys)
			}
			// find out which simple columns changed
			val columnsChanged = table.simpleTypeNotAutoGeneratedColumns.filter(changed _)

			// if there is a change, update it
			val args = newValuesMap.toListOfColumnAndValueTuple(columnsChanged) ::: pluginDUR.values
			if (!args.isEmpty) {
				val pkArgs = oldValuesMap.toListOfColumnAndValueTuple(table.primaryKeys) ::: pluginDUR.keys
				// execute the before update events
				events.executeBeforeUpdateEvents(tpe, args, pkArgs)

				driver.doUpdate(tpe, args, pkArgs)

				// execute the after update events
				events.executeAfterUpdateEvents(tpe, args, pkArgs)
			}

			// update the mock
			mockO = createMock(entity, modified ++ modifiedTraversables)
			entityMap.put(o, mockO)

			postUpdatePlugins.foreach { plugin =>
				plugin.after(updateConfig, entity, o, mockO, oldValuesMap, newValuesMap, entityMap, modifiedTraversables)
			}

			// done, construct the updated entity
			val finalValuesMap = ValuesMap.fromMutableMap(typeManager, modified ++ modifiedTraversables)
			val v = tpe.constructor(finalValuesMap)
			entityMap.put(o, v)
			v
		}

	/**
	 * update an entity. The entity must have been retrieved from the database and then
	 * changed prior to calling this method.
	 * The whole object graph will be updated (if necessary).
	 */
	def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC): T with PC =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't update an object that is not persisted: " + o);
			val persisted = o.asInstanceOf[T with PC with Persisted]
			validatePersisted(persisted)
			val entityMap = new UpdateEntityMap
			try {
				val v = updateInner(updateConfig, entity, o, entityMap)
				entityMap.done
				v
			} catch {
				case e: Throwable => throw new PersistException("An error occured during update of entity %s with value %s.".format(entity, o), e)
			}

		}

	private[mapperdao] def updateInner[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC, entityMap: UpdateEntityMap): T with PC with Persisted =
		// do a check if a mock is been updated
		o match {
			case p: Persisted if (p.mock) =>
				val v = o.asInstanceOf[T with PC with Persisted]
				// report an error if mock was changed by the user
				val tpe = entity.tpe
				val newVM = ValuesMap.fromEntity(typeManager, tpe, o, false)
				val oldVM = v.valuesMap
				if (newVM.isSimpleColumnsChanged(tpe, oldVM)) throw new IllegalStateException("please don't modify mock objects. Object %s is mock and has been modified.".format(p))
				v
			case _ =>
				// if a mock exists in the entity map or already persisted, then return
				// the existing mock/persisted object
				entityMap.get[PC, T](o).getOrElse {
					val persisted = o.asInstanceOf[T with PC with Persisted]
					val oldValuesMap = persisted.valuesMap
					val tpe = entity.tpe
					val newValuesMapPre = ValuesMap.fromEntity(typeManager, tpe, o)
					val reConstructed = tpe.constructor(newValuesMapPre)
					updateInner(updateConfig, entity, o, oldValuesMap, reConstructed.valuesMap, entityMap)
				}
		}
	/**
	 * update an immutable entity. The entity must have been retrieved from the database. Because immutables can't change, a new instance
	 * of the entity must be created with the new values prior to calling this method. Values that didn't change should be copied from o.
	 * For traversables, the method heavily relies on object equality to assess which entities will be updated. So please copy over
	 * traversable entities from the old collections to the new ones (but you can instantiate a new collection).
	 *
	 * The whole tree will be updated (if necessary).
	 *
	 * @param	o		the entity, as retrieved from the database
	 * @param	newO	the new instance of the entity with modifications. The database will be updated
	 * 					based on differences between newO and o
	 * @return			The updated entity. Both o and newO should be disposed (not used) after the call.
	 */
	def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC, newO: T): T with PC =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't update an object that is not persisted: " + o);
			val persisted = o.asInstanceOf[T with PC with Persisted]
			validatePersisted(persisted)
			persisted.discarded = true
			try {
				val entityMap = new UpdateEntityMap
				val v = updateInner(updateConfig, entity, persisted, newO, entityMap)
				entityMap.done
				v
			} catch {
				case e => throw new PersistException("An error occured during update of entity %s with old value %s and new value %s".format(entity, o, newO), e)
			}
		}

	private[mapperdao] def updateInner[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC with Persisted, newO: T, entityMap: UpdateEntityMap): T with PC =
		{
			val oldValuesMap = o.valuesMap
			val newValuesMap = ValuesMap.fromEntity(typeManager, entity.tpe, newO)
			updateInner(updateConfig, entity, newO, oldValuesMap, newValuesMap, entityMap)
		}

	private def validatePersisted(persisted: Persisted) {
		if (persisted.discarded) throw new IllegalArgumentException("can't operate on an object twice. An object that was updated/deleted must be discarded and replaced by the return value of update(), i.e. onew=update(o) or just be disposed if it was deleted. The offending object was : " + persisted);
		if (persisted.mock) throw new IllegalArgumentException("can't operate on a 'mock' object. Mock objects are created when there are cyclic dependencies of entities, i.e. entity A depends on B and B on A on a many-to-many relationship.  The offending object was : " + persisted);
	}

	/**
	 * select an entity but load only part of the entity's graph. SelectConfig contains configuration regarding which relationships
	 * won't be loaded, i.e.
	 *
	 * SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
	 */
	def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC] =
		{
			val entityMap = new EntityMap
			val v = selectInner(entity, selectConfig, ids, entityMap)
			entityMap.done
			v
		}

	private[mapperdao] def selectInner[PC, T](entity: Entity[PC, T], selectConfig: SelectConfig, ids: List[Any], entities: EntityMap): Option[T with PC] =
		{
			val clz = entity.clz
			val tpe = entity.tpe
			if (tpe.table.primaryKeys.size != ids.size) throw new IllegalStateException("Primary keys number dont match the number of parameters. Primary keys: %s".format(tpe.table.primaryKeys))

			entities.get[T with PC](tpe.clz, ids).headOption.orElse(
				try {
					val args = tpe.table.primaryKeys.map(_.column).zip(ids)
					events.executeBeforeSelectEvents(tpe, args)
					val om = driver.doSelect(tpe, args)
					events.executeAfterSelectEvents(tpe, args)
					if (om.isEmpty) None
					else if (om.size > 1) throw new IllegalStateException("expected 1 result for %s and ids %s, but got %d. Is the primary key column a primary key in the table?".format(clz.getSimpleName, ids, om.size))
					else {
						val l = toEntities(om, entity, selectConfig, entities)
						if (l.size != 1) throw new IllegalStateException("expected 1 object, but got %s".format(l))
						Some(l.head)
					}
				} catch {
					case e => throw new QueryException("An error occured during select of entity %s and primary keys %s".format(entity, ids), e)
				}
			)
		}

	private[mapperdao] def toEntities[PC, T](lm: List[JdbcMap], entity: Entity[PC, T], selectConfig: SelectConfig, entities: EntityMap): List[T with PC] = lm.map { om =>
		val mods = new scala.collection.mutable.HashMap[String, Any]
		import scala.collection.JavaConversions._
		mods ++= om.map.toMap
		val tpe = entity.tpe
		val table = tpe.table
		// calculate the id's for this tpe
		val ids = table.primaryKeys.map { pk => om(pk.column.columnName) } ::: selectBeforePlugins.map { plugin =>
			plugin.idContribution(tpe, om, entities, mods)
		}.flatten
		val cacheKey = if (ids.isEmpty) {
			if (table.unusedPKs.isEmpty) {
				throw new IllegalStateException("entity %s without primary key, please use declarePrimaryKeys() to declare the primary key columns of tables into your entity declaration")
			} else {
				table.unusedPKs.map { pk => om(pk.columnName) }
			}
		} else ids

		entities.get[T with PC](tpe.clz, cacheKey).getOrElse {
			val mock = createMock(entity, mods)
			entities.put(tpe.clz, cacheKey, mock)

			selectBeforePlugins.foreach { plugin =>
				plugin.before(entity, selectConfig, om, entities, mods)
			}

			val vm = ValuesMap.fromMutableMap(typeManager, mods)
			val entityV = tpe.constructor(vm)
			entities.reput(tpe.clz, cacheKey, entityV)
			entityV
		}
	}

	/**
	 * creates a mock object
	 */
	private def createMock[PC, T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any]): T with PC with Persisted =
		{
			val mockMods = new scala.collection.mutable.HashMap[String, Any]
			mockMods ++= mods
			mockPlugins.foreach { plugin =>
				plugin.updateMock(entity, mockMods)
			}
			val tpe = entity.tpe
			// create a mock of the final entity, to avoid cyclic dependencies
			val preMock = tpe.constructor(ValuesMap.fromMutableMap(typeManager, mockMods))
			val mock = tpe.constructor(ValuesMap.fromEntity(typeManager, tpe, preMock))
			// mark it as mock
			mock.mock = true
			mock
		}

	/**
	 * deletes an entity from the database
	 */
	def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T = {
		val entityMap = new UpdateEntityMap
		val deleted = deleteInner(deleteConfig, entity, o, entityMap)
		entityMap.done
		deleted
	}

	private[mapperdao] def deleteInner[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC, entityMap: UpdateEntityMap): T =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't delete an object that is not persisted: " + o);

			val persisted = o.asInstanceOf[T with PC with Persisted]
			if (persisted.discarded) throw new IllegalArgumentException("can't operate on an object twice. An object that was updated/deleted must be discarded and replaced by the return value of update(), i.e. onew=update(o) or just be disposed if it was deleted. The offending object was : " + o);
			persisted.discarded = true

			val tpe = entity.tpe
			val table = tpe.table

			try {
				val keyValues0 = table.toListOfPrimaryKeySimpleColumnAndValueTuples(o) ::: beforeDeletePlugins.flatMap(plugin =>
					plugin.idColumnValueContribution(tpe, deleteConfig, events, persisted, entityMap)
				)

				val unusedPKColumns = table.unusedPKs.filterNot(unusedColumn => keyValues0.map(_._1).contains(unusedColumn))
				val keyValues = keyValues0 ::: table.toListOfColumnAndValueTuples(unusedPKColumns, o)
				// call all the before-delete plugins
				beforeDeletePlugins.foreach { plugin =>
					plugin.before(entity, deleteConfig, events, persisted, keyValues, entityMap)
				}

				// execute the before-delete events
				events.executeBeforeDeleteEvents(tpe, keyValues, o)

				// do the actual delete database op
				driver.doDelete(tpe, keyValues)

				// execute the after-delete events
				events.executeAfterDeleteEvents(tpe, keyValues, o)

				// return the object
				o
			} catch {
				case e => throw new PersistException("An error occured during delete of entity %s with value %s".format(entity, o), e)
			}

		}
	/**
	 * ===================================================================================
	 * common methods
	 * ===================================================================================
	 */
	override def toString = "MapperDao(%s)".format(driver)
}

object MapperDao {
	def apply(driver: Driver): MapperDaoImpl = new MapperDaoImpl(driver, new Events)
	def apply(driver: Driver, events: Events): MapperDaoImpl = new MapperDaoImpl(driver, events)
}

/**
 * a mock impl of the mapperdao trait, to be used for testing
 */
class MockMapperDao extends MapperDao {
	// insert
	override def insert[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T): T with PC = null.asInstanceOf[T with PC]

	// update
	override def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC): T with PC = null.asInstanceOf[T with PC]
	// update immutable
	override def update[PC, T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T with PC, newO: T): T with PC = null.asInstanceOf[T with PC]

	// select
	override def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC] = None

	// delete
	override def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T = null.asInstanceOf[T]
}