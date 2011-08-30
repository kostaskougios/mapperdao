package com.rits.orm

import com.rits.jdbc.Jdbc
import com.rits.orm.drivers.Driver
import scala.collection.mutable.HashMap
import com.rits.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 13 Jul 2011
 */
final class MapperDao(val driver: Driver) {
	val typeRegistry = driver.typeRegistry
	val typeManager = driver.jdbc.typeManager
	/**
	 * ===================================================================================
	 * Utility methods
	 * ===================================================================================
	 */

	private def isPersisted(o: Any): Boolean = o.isInstanceOf[Persisted]

	private def addToMap(key: String, v: Any, m: scala.collection.mutable.Map[String, List[Any]]) =
		{
			var l = m.getOrElse(key, List[Any]())
			l ::= v
			m(key) = l
			l
		}
	/**
	 * ===================================================================================
	 * CRUD OPERATIONS
	 * ===================================================================================
	 */

	private def insertInner[PC, T, P](entity: Entity[PC, T], o: T, parent: P, parentColumn: ColumnBase, parentKeysAndValues: List[(SimpleColumn, Any)], entityMap: UpdateEntityMap): T with PC =
		{
			val tpe = typeRegistry.typeOf(entity)
			val table = tpe.table
			// if a mock exists in the entity map or already persisted, then return
			// the existing mock/persisted object
			val mock = entityMap.get[PC, T](o)
			if (mock.isDefined) return mock.get

			if (o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't insert an object that is already persisted: " + o);

			val modified = ValuesMap.fromEntity(typeManager, tpe, o).toMutableMap
			val modifiedTraversables = new HashMap[String, List[Any]]
			// extra args for foreign keys
			var extraArgs = if (parent != null) {
				// parent of the one-to-many
				val pti = typeRegistry.entityOf[Any, P](parent)
				val table = typeRegistry.typeOf(pti).table
				parentColumn match {
					case otm: OneToMany[_] =>
						val foreignKeyColumns = otm.foreignColumns
						val foreignKeys = parentKeysAndValues.map(_._2)
						if (foreignKeys.size != foreignKeyColumns.size) throw new IllegalArgumentException("mappings of one-to-many from " + parent + " to " + o + " is invalid. Number of FK columns doesn't match primary keys. columns: " + foreignKeyColumns + " , primary key values " + foreignKeys);
						foreignKeyColumns.zip(foreignKeys)
					case oto: OneToOneReverse[T] =>
						oto.foreignColumns zip parentKeysAndValues.map(_._2)
				}
			} else Nil

			// many-to-one
			table.manyToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOf[Any, Any](fo)
					val v = fo match {
						case null => null
						case p: Persisted =>
							update(fe, p)
						case x =>
							insert(fe, x)
					}
					extraArgs :::= cis.column.columns zip typeRegistry.typeOf(fe).table.toListOfPrimaryKeyValues(v)
					v
				} else null
				modified(cis.column.alias) = v
			}
			// arguments
			val args = table.toListOfColumnAndValueTuples(table.simpleTypeNotAutoGeneratedColumns, o) ::: extraArgs

			if (!args.isEmpty || !table.simpleTypeAutoGeneratedColumns.isEmpty) {
				val ur = driver.doInsert(tpe, args)

				table.simpleTypeAutoGeneratedColumns.foreach { c =>
					modified(c.columnName) = ur.keys.get(c.columnName).get
				}
			}

			val newKeyValues = table.primaryKeys.map(c => modified(c.columnName))
			// put a mock into the entity map
			val mockO = tpe.constructor(ValuesMap.fromMutableMap(typeManager, modified ++ modifiedTraversables))
			mockO.mock = true
			entityMap.put(o, mockO)

			lazy val newKeyColumnAndValues = table.primaryKeys.map(_.column) zip newKeyValues

			// one-to-one
			table.oneToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOf[Any, Any](fo)
					fo match {
						case null => null
						case p: Persisted =>
							update(fe, p)
						case x =>
							insertInner(fe, x, o, cis.column, newKeyColumnAndValues, entityMap)
					}
				} else null
				modified(cis.column.alias) = v
			}
			// one-to-one reverse
			table.oneToOneReverseColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOf[Any, Any](fo)
					fo match {
						case null => null
						case p: Persisted =>
							update(fe, p)
						case x =>
							insertInner(fe, x, o, cis.column, newKeyColumnAndValues, entityMap)
					}
				} else null
				modified(cis.column.alias) = v
			}
			// one to many
			table.oneToManyColumnInfos.foreach { cis =>
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOf[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (isPersisted(nested)) {
							val OneToMany(foreign: TypeRef[_], foreignColumns: List[Column]) = cis.column
							// update
							val keyArgs = nestedTpe.table.toListOfColumnAndValueTuples(nestedTpe.table.primaryKeys, nested)
							driver.doUpdateOneToManyRef(nestedTpe, foreignColumns zip newKeyValues, keyArgs)
							nested
						} else {
							// insert
							insertInner(nestedEntity, nested, o, cis.column, newKeyColumnAndValues, entityMap)
						}
						val cName = cis.column.alias
						addToMap(cName, newO, modifiedTraversables)
					}
				}
			}

			// many to many
			table.manyToManyColumnInfos.foreach { cis =>
				val traversable = cis.columnToValue(o)
				if (traversable != null) {
					traversable.foreach { nested =>
						val nestedEntity = typeRegistry.entityOf[Any, Any](nested)
						val nestedTpe = typeRegistry.typeOf(nestedEntity)
						val newO = if (isPersisted(nested)) {
							nested
						} else {
							insert(nestedEntity, nested)
						}
						val rightKeyValues = nestedTpe.table.toListOfPrimaryKeyAndValueTuples(newO)
						driver.doInsertManyToMany(nestedTpe, cis.column, newKeyColumnAndValues, rightKeyValues)
						val cName = cis.column.alias
						addToMap(cName, newO, modifiedTraversables)
					}
				}
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
	def insert[PC, T](entity: Entity[PC, T], o: T): T with PC =
		{
			insertInner[PC, T, Any](entity, o, null, null, null, new UpdateEntityMap)
		}
	/**
	 * update an entity
	 */

	private def updateInner[PC, T](entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap): T with PC =
		{
			val tpe = typeRegistry.typeOf(typeRegistry.entityOf[PC, T](o))
			val table = tpe.table

			val modified = oldValuesMap.toMutableMap ++ newValuesMap.toMutableMap
			val modifiedTraversables = new HashMap[String, List[Any]]

			def onlyChanged(column: ColumnBase) = newValuesMap(column.alias) != oldValuesMap(column.alias)

			// first, lets update the simple types that changed
			val columnsChanged = table.simpleTypeNotAutoGeneratedColumns.filter(onlyChanged _)
			val manyToOneChanged = table.manyToOneColumns.filter(onlyChanged _)
			if (!columnsChanged.isEmpty || !manyToOneChanged.isEmpty) {
				val mtoArgsV = manyToOneChanged.map(mto => (mto.foreign.clz, newValuesMap[Any](mto.alias))).map { t =>
					typeRegistry.typeOf(typeRegistry.entityOf[Any, Any](t._1)).table.toListOfPrimaryKeyValues(t._2)
				}.flatten
				val mtoArgs = manyToOneChanged.map(_.columns).flatten zip mtoArgsV
				val args = newValuesMap.toListOfColumnAndValueTuple(columnsChanged) ::: mtoArgs
				val pkArgs = oldValuesMap.toListOfColumnAndValueTuple(table.primaryKeys)
				driver.doUpdate(tpe, args, pkArgs)
			}

			// next, update one-to-many
			table.oneToManyColumns.foreach { oneToMany =>
				val t: Traversable[Any] = table.oneToManyToColumnInfoMap(oneToMany).columnToValue(o)

				// we'll get the 2 traversables and update the database
				// based on their differences
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](oneToMany.foreign.alias)

				// update those that remained in the updated traversable
				val intersection = newValues.intersect(oldValues)
				intersection.foreach { item =>
					val fe = typeRegistry.entityOf[Any, Any](item)
					val newItem = update(fe, item)
					item.asInstanceOf[Persisted].discarded = true
					addToMap(oneToMany.alias, newItem, modifiedTraversables)
				}
				// find the added ones
				val diff = newValues.diff(oldValues)
				diff.foreach { item =>
					val keysAndValues = table.primaryKeys.map(_.column) zip table.primaryKeys.map(c => modified(c.columnName))
					val fe = typeRegistry.entityOf(item)
					val newItem: Any = insertInner(fe, item, o, oneToMany, keysAndValues, entityMap);
					addToMap(oneToMany.alias, newItem, modifiedTraversables)
				}

				// find the removed ones
				val odiff = oldValues.diff(newValues)
				odiff.foreach { item =>
					val fe = typeRegistry.entityOf[Any, Any](item)
					delete(fe, item)
				}
			}

			// update many-to-many
			table.manyToManyColumns.foreach { manyToMany =>
				val t = table.manyToManyToColumnInfoMap(manyToMany).columnToValue(o)
				val newValues = t.toList
				val oldValues = oldValuesMap.seq[Any](manyToMany.foreign.alias)

				val pkArgs = manyToMany.linkTable.left zip oldValuesMap.toListOfColumnValue(table.primaryKeys)

				// update those that remained in the updated traversable
				val intersection = newValues.intersect(oldValues)
				intersection.foreach { item =>
					val newItem = item match {
						case p: Persisted if (!p.mock) =>
							val fe = typeRegistry.entityOf[Any, Any](item)
							update(fe, item)
							p.discarded = true
						case _ => item
					}
					addToMap(manyToMany.alias, newItem, modifiedTraversables)
				}

				// find the added ones
				val diff = newValues.diff(oldValues)
				diff.foreach { item =>
					val newItem = item match {
						case p: Persisted => p
						case n => insert[Any, Any](typeRegistry.entityOf(n), n)
					}
					val ftpe = typeRegistry.typeOf(newItem)
					val fPKArgs = manyToMany.linkTable.right zip ftpe.table.toListOfPrimaryKeyValues(newItem)
					driver.doInsertManyToMany(tpe, manyToMany, pkArgs, fPKArgs)
					addToMap(manyToMany.alias, newItem, modifiedTraversables)
				}
				// find the removed ones
				val odiff = oldValues.diff(newValues)
				odiff.foreach(_ match {
					case p: Persisted =>
						val ftpe = typeRegistry.typeOf[Any, Any](p)
						val ftable = ftpe.table
						val fPkArgs = manyToMany.linkTable.right zip ftable.toListOfPrimaryKeyValues(p)
						driver.doDeleteManyToManyRef(tpe, ftpe, manyToMany, pkArgs, fPkArgs)
						p.discarded = true
				})

			}
			// now update o.valuesMap
			val finalValuesMap = ValuesMap.fromMutableMap(typeManager, modified ++ modifiedTraversables)
			val r = tpe.constructor(finalValuesMap)
			r
		}

	/**
	 * update an entity. The entity must have been retrieved from the database and then
	 * changed prior to calling this method.
	 * The whole tree will be updated (if necessary).
	 * The method heavily relies on object equality to assess which entities will be updated.
	 */
	def update[PC, T](entity: Entity[PC, T], o: T with PC): T with PC =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't update an object that is not persisted: " + o);
			val persisted = o.asInstanceOf[T with PC with Persisted]
			validatePersisted(persisted)
			val oldValuesMap = persisted.valuesMap
			val newValuesMap = ValuesMap.fromEntity(typeManager, typeRegistry.typeOf(o), o)
			updateInner(entity, o, oldValuesMap, newValuesMap, new UpdateEntityMap)
		}

	/**
	 * update an immutable entity. The entity must have been retrieved from the database. Because immutables can't change, a new instance
	 * of the entity must be created with the new values prior to calling this method. Values that didn't change should be copied from o.
	 * The method heavily relies on object equality to assess which entities will be updated.
	 * The whole tree will be updated (if necessary).
	 *
	 * @param	o		the entity, as retrieved from the database
	 * @param	newO	the new instance of the entity with modifications. The database will be updated
	 * 					based on differences between newO and o
	 * @return			The updated entity. Both o and newO should be disposed (not used) after the call.
	 */
	def update[PC, T](entity: Entity[PC, T], o: T with PC, newO: T): T with PC =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't update an object that is not persisted: " + o);
			val persisted = o.asInstanceOf[Persisted]
			validatePersisted(persisted)
			persisted.discarded = true
			val oldValuesMap = persisted.valuesMap
			val newValuesMap = ValuesMap.fromEntity(typeManager, typeRegistry.typeOf(newO), newO)
			updateInner(entity, newO, oldValuesMap, newValuesMap, new UpdateEntityMap)
		}

	private def validatePersisted(persisted: Persisted) {
		if (persisted.discarded) throw new IllegalArgumentException("can't operate on an object twice. An object that was updated/deleted must be discarded and replaced by the return value of update(), i.e. onew=update(o) or just be disposed if it was deleted. The offending object was : " + persisted);
		if (persisted.mock) throw new IllegalArgumentException("can't operate on a 'mock' object. Mock objects are created when there are cyclic dependencies of entities, i.e. entity A depends on B and B on A on a many-to-many relationship.  The offending object was : " + persisted);
	}
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

	def select[PC, T](entity: Entity[PC, T], ids: List[Any]): Option[T with PC] =
		{
			select(entity, ids, new EntityMap)
		}
	private def select[PC, T](entity: Entity[PC, T], ids: List[Any], entities: EntityMap): Option[T with PC] =
		{
			val clz = entity.clz
			val tpe = typeRegistry.typeOf(entity)
			if (tpe.table.primaryKeys.size != ids.size) throw new IllegalStateException("Primary keys number dont match the number of parameters. Primary keys: %s".format(tpe.table.primaryKeys))

			val om = driver.doSelect(tpe, tpe.table.primaryKeys.map(_.column).zip(ids))
			if (om.isEmpty) None
			else if (om.size > 1) throw new IllegalStateException("expected 1 result for %s and ids %s, but got %d. Is the primary key column a primary key in the table?".format(clz.getSimpleName, ids, om.size))
			else {
				val l = toEntities(om, tpe, entities)
				if (l.size != 1) throw new IllegalStateException("expected 1 object, but got %s".format(l))
				Option(l.head)
			}
		}

	protected[orm] def toEntities[PC, T](lm: List[JdbcMap], tpe: Type[PC, T], entities: EntityMap): List[T with PC] = lm.map { om =>
		val mods = new scala.collection.mutable.HashMap[String, Any]
		mods ++= om.map
		val table = tpe.table
		// calculate the id's for this tpe
		val ids: List[Any] = tpe.table.primaryKeys.map { pk => om(pk.column.columnName) }
		val entity = entities.get[T with PC](tpe.clz, ids)
		if (entity.isDefined) {
			entity.get
		} else {
			def createMock: T with Persisted =
				{
					mods ++= table.oneToManyColumns.map(c => (c.alias -> List()))
					mods ++= table.manyToManyColumns.map(c => (c.alias -> List()))
					// create a mock of the final entity, to avoid cyclic dependencies
					val mock = tpe.constructor(ValuesMap.fromMutableMap(typeManager, mods))
					// mark it as mock
					mock.mock = true
					mock
				}
			// this mock object is updated with any changes that follow
			val mock = createMock
			entities.put(tpe.clz, ids, mock)

			// one to one reverse
			table.oneToOneReverseColumns.foreach { c =>
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val fom = driver.doSelect(ftpe, c.foreignColumns.zip(ids))
				val otmL = toEntities(fom, ftpe, entities)
				if (otmL.size != 1) throw new IllegalStateException("expected 1 row but got " + otmL);
				mods(c.foreign.alias) = otmL.head
			}
			// many to one
			table.manyToOneColumns.foreach { c =>
				val fe = typeRegistry.entityOf[Any, Any](c.foreign.clz)
				val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
				val fo = entities.get(fe.clz, foreignPKValues)
				val v = if (fo.isDefined) {
					fo.get
				} else {
					select(fe, foreignPKValues, entities).getOrElse(null)
				}
				mods(c.foreign.alias) = v
			}
			// one to many
			table.oneToManyColumns.foreach { c =>
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val fom = driver.doSelect(ftpe, c.foreignColumns.zip(ids))
				val otmL = toEntities(fom, ftpe, entities)
				mods(c.foreign.alias) = otmL
			}

			// many to many
			table.manyToManyColumns.foreach { c =>
				val ftpe = typeRegistry.typeOf(c.foreign.clz)
				val fom = driver.doSelectManyToMany(tpe, ftpe, c, c.linkTable.left zip ids)
				val mtmR = toEntities(fom, ftpe, entities)
				mods(c.foreign.alias) = mtmR
			}

			val vm = ValuesMap.fromMutableMap(typeManager, mods)
			mock.valuesMap.m = vm.m
			val entity = tpe.constructor(vm)
			entities.reput(tpe.clz, ids, entity)
			entity
		}
	}

	/**
	 * deletes an entity from the database
	 */
	def delete[PC, T](entity: Entity[PC, T], o: T with PC): T =
		{
			if (!o.isInstanceOf[Persisted]) throw new IllegalArgumentException("can't delete an object that is not persisted: " + o);

			val persisted = o.asInstanceOf[Persisted]
			if (persisted.discarded) throw new IllegalArgumentException("can't operate on an object twice. An object that was updated/deleted must be discarded and replaced by the return value of update(), i.e. onew=update(o) or just be disposed if it was deleted. The offending object was : " + o);
			persisted.discarded = true

			val tpe = typeRegistry.typeOf(entity)
			val table = tpe.table

			val keyValues = table.toListOfPrimaryKeyAndValueTuples(o)
			driver.doDelete(tpe, keyValues)
			o
		}
	/**
	 * ===================================================================================
	 * ID helper methods
	 * ===================================================================================
	 */
	/**
	 * retrieve the id of an entity
	 */
	def intIdOf(o: AnyRef) = o match {
		case iid: IntId => iid.id
	}

	/**
	 * retrive the id of an entity
	 */
	def longIdOf(o: AnyRef) = o match {
		case iid: LongId => iid.id
	}

	/**
	 * ===================================================================================
	 * common methods
	 * ===================================================================================
	 */
	override def toString = "MapperDao(%s)".format(driver)
}
