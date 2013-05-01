package com.googlecode.mapperdao.jdbc.impl

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.exceptions._
import com.googlecode.mapperdao.plugins._
import com.googlecode.mapperdao.utils.{UnlinkEntityRelationshipVisitor, Helpers}
import com.googlecode.mapperdao.state.persistcmds.CmdPhase
import com.googlecode.mapperdao.state.recreation.MockFactory
import com.googlecode.mapperdao.state.recreation.RecreationPhase
import com.googlecode.mapperdao.state.prioritise.PriorityPhase
import com.googlecode.mapperdao.internal.UpdateEntityMap
import com.googlecode.mapperdao.lazyload.LazyLoadManager
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.enhancevm.EnhanceVMPhase
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao.jdbc.{DatabaseValues, CmdToDatabase}
import com.googlecode.mapperdao.schema.ColumnInfoTraversableManyToMany
import scala.Some
import com.googlecode.mapperdao.schema.ColumnInfoManyToOne
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.schema.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.plugins.SelectMod

/**
 * @author kostantinos.kougios
 *
 *         13 Jul 2011
 */
protected final class MapperDaoImpl(
	val driver: Driver,
	val typeManager: TypeManager
	) extends MapperDao
{
	private val typeRegistry = driver.typeRegistry
	private val lazyLoadManager = new LazyLoadManager
	private val mockFactory = new MockFactory(typeManager)

	private val selectBeforePlugins: List[BeforeSelect] = List(
		new ManyToOneSelectPlugin(typeRegistry, this),
		new OneToManySelectPlugin(typeRegistry, driver, this),
		new OneToOneReverseSelectPlugin(typeRegistry, driver, this),
		new OneToOneSelectPlugin(typeRegistry, driver, this),
		new ManyToManySelectPlugin(typeRegistry, driver, this)
	)

	private val beforeDeletePlugins: List[BeforeDelete] = List(
		new ManyToManyDeletePlugin(driver, this),
		new OneToManyDeletePlugin(typeRegistry, this),
		new OneToOneReverseDeletePlugin(typeRegistry, driver, this),
		new ManyToOneDeletePlugin
	)

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

	override def insert0[ID, T](
		updateConfig: UpdateConfig,
		tpe: Type[ID, T],
		os: List[T]
		) = {
		val po = new CmdPhase(typeManager)
		val cmds = os.map {
			o =>
				if (isPersisted(o)) throw new IllegalArgumentException("can't insert an object that is already persisted: " + o)
				val newVM = ValuesMap.fromType(typeManager, tpe, o)
				po.toInsertCmd(tpe, newVM, updateConfig)
		}.flatten
		val pf = new PriorityPhase(updateConfig)
		val pri = pf.prioritise(tpe, cmds)

		val ctd = new CmdToDatabase(updateConfig, driver, typeManager, pri)
		val nodes = ctd.execute

		val enhanceVM = new EnhanceVMPhase
		enhanceVM.execute(pri)

		val recreationPhase = new RecreationPhase(updateConfig, mockFactory, typeManager, new UpdateEntityMap, nodes)
		val recreated = recreationPhase.execute.asInstanceOf[List[T with DeclaredIds[ID]]]
		recreated
	}

	override def updateMutable0[ID, T](
		updateConfig: UpdateConfig,
		tpe: Type[ID, T],
		os: List[T with Persisted]
		): List[T with Persisted] = {
		val osAndNewValues = os.map {
			o =>
				o match {
					case p: Persisted if (p.mapperDaoValuesMap.mock) =>
						throw new IllegalStateException("Object %s is mock.".format(p))
					case persisted: Persisted =>
						val newValuesMap = ValuesMap.fromType(typeManager, tpe, o, persisted.mapperDaoValuesMap)
						(o, newValuesMap)
				}
		}
		updateProcess(updateConfig, tpe, osAndNewValues)
	}

	override def updateImmutable0[ID, T](
		updateConfig: UpdateConfig,
		tpe: Type[ID, T],
		os: List[(T with Persisted, T)]
		): List[T with Persisted] = {
		val osAndNewValues = os.map {
			case (oldO, newO) =>
				oldO.mapperDaoDiscarded = true
				val newVM = ValuesMap.fromType(typeManager, tpe, newO, oldO.mapperDaoValuesMap)
				(oldO, newVM)
		}
		updateProcess(updateConfig, tpe, osAndNewValues)
	}

	private def updateProcess[ID, T](
		updateConfig: UpdateConfig,
		tpe: Type[ID, T],
		os: List[(T with Persisted, ValuesMap)]
		): List[T with Persisted] = {

		val po = new CmdPhase(typeManager)
		val cmds = os.map {
			case (o, newVM) =>
				val oldVM = o.mapperDaoValuesMap
				po.toUpdateCmd(tpe, oldVM, newVM, updateConfig)
		}.flatten

		val pf = new PriorityPhase(updateConfig)
		val pri = pf.prioritise(tpe, cmds)

		val ctd = new CmdToDatabase(updateConfig, driver, typeManager, pri)
		val nodes = ctd.execute

		val enhanceVM = new EnhanceVMPhase
		enhanceVM.execute(pri)

		val recreationPhase = new RecreationPhase(updateConfig, mockFactory, typeManager, new UpdateEntityMap, nodes)
		recreationPhase.execute.asInstanceOf[List[T with Persisted]]
	}

	/**
	 * select an entity but load only part of the entity's graph. SelectConfig contains configuration regarding which relationships
	 * won't be loaded, i.e.
	 *
	 * SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
	 */
	override def select0[ID, T](selectConfig: SelectConfig, entity: Entity[ID, Persisted, T], id: ID) = {
		if (id == null) throw new NullPointerException("ids can't be null")
		val ids = Helpers.idToList(id)
		val pkSz = entity.tpe.table.primaryKeysSize
		if (pkSz != ids.size) throw new IllegalArgumentException("entity has %d keys, can't use these keys: %s".format(pkSz, ids))
		val entityMap = new EntityMap
		val v = selectInner(entity, selectConfig, ids, entityMap)
		v
	}

	private[mapperdao] def selectInner[ID, T](
		entity: EntityBase[ID, T],
		selectConfig: SelectConfig,
		ids: List[Any],
		entities: EntityMap
		): Option[T with Persisted] = {
		val clz = entity.clz
		val tpe = entity.tpe
		if (tpe.table.primaryKeysSize != ids.size) throw new IllegalStateException("Primary keys number dont match the number of parameters. Primary keys: %s".format(tpe.table.primaryKeys))

		entities.get[T with Persisted](tpe.clz, ids) {
			try {
				val (pks, declared) = ids.splitAt(tpe.table.primaryKeys.size)
				val pkArgs = tpe.table.primaryKeys.zip(pks)
				// convert unused keys to their simple values
				val declaredArgs = if (tpe.table.unusedPKs.isEmpty)
					Nil
				else
					(
						(tpe.table.unusedPKColumnInfos zip declared) map {
							case (ci, v) =>
								ci match {
									case ci: ColumnInfoManyToOne[T, Any, Any] =>
										val foreign = ci.column.foreign
										val fentity = foreign.entity
										val ftable = fentity.tpe.table
										ci.column.columns zip ftable.toListOfPrimaryKeyValues(v)
									case ci: ColumnInfoTraversableOneToMany[Any, Any, Any, Any] =>
										val fentity = ci.entityOfT
										val ftable = fentity.tpe.table
										ci.column.columns zip ftable.toListOfPrimaryKeyValues(v)
									case ci: ColumnInfoOneToOne[T, Any, Any] =>
										val foreign = ci.column.foreign
										val fentity = foreign.entity
										val ftable = fentity.tpe.table
										ci.column.columns zip ftable.toListOfPrimaryKeyValues(v)
									case _ => throw new IllegalArgumentException("Please use declarePrimaryKey only for relationships. For normal data please use key(). This occured for entity %s".format(entity.getClass))
								}
						}).flatten

				val args = pkArgs ::: declaredArgs

				val om = driver.doSelect(selectConfig, tpe, args)
				if (om.isEmpty) None
				else if (om.size > 1) throw new IllegalStateException("expected 1 result for %s and ids %s, but got %d. Is the primary key column a primary key in the table?".format(clz.getSimpleName, ids, om.size))
				else {
					val l = toEntities(om, entity, selectConfig, entities)
					Some(l.head)
				}
			} catch {
				case e: Throwable => throw new QueryException("An error occured during select of entity %s and primary keys %s".format(entity, ids), e)
			}
		}
	}

	private[mapperdao] def toEntities[ID, T](
		lm: List[DatabaseValues],
		entity: EntityBase[ID, T],
		selectConfig: SelectConfig,
		entities: EntityMap
		): List[T with Persisted] = {
		lm.map {
			jdbcMap =>
				val tpe = entity.tpe
				val table = tpe.table
				// calculate the id's for this tpe
				val pkIds = table.primaryKeys.map {
					pk => jdbcMap(pk)
				} ::: selectBeforePlugins.map {
					_.idContribution(tpe, jdbcMap, entities)
				}.flatten
				val unusedIds = table.unusedPKs.map {
					pk =>
						jdbcMap(pk)
				}
				val ids = pkIds ::: unusedIds
				if (ids.isEmpty)
					throw new IllegalStateException("entity %s without primary key, please use declarePrimaryKeys() to declare the primary key columns of tables into your entity declaration")

				entities.get[T with Persisted](tpe.clz, ids) {
					val mods = jdbcMap.toMap
					val mock = mockFactory.createMock(selectConfig.data, entity.tpe, mods)
					entities.putMock(tpe.clz, ids, mock)

					val allMods = mods ++ selectBeforePlugins.map {
						_.before(entity, selectConfig, jdbcMap, entities)
					}.flatten.map {
						case SelectMod(k, v, lazyBeforeLoadVal) =>
							(k, v)
					}.toMap

					val vm = ValuesMap.fromMap(null, allMods)
					// if the entity should be lazy loaded and it has relationships, then
					// we need to lazy load it
					val entityV = if (lazyLoadManager.isLazyLoaded(selectConfig.lazyLoad, entity)) {
						lazyLoadEntity(entity, selectConfig, vm)
					} else tpe.constructor(selectConfig.data, vm)
					vm.o = entityV
					Some(entityV)
				}.get
		}
	}

	private def lazyLoadEntity[ID, T](
		entity: EntityBase[ID, T],
		selectConfig: SelectConfig,
		vm: ValuesMap
		) = {
		// substitute lazy loaded columns with empty values
		val tpe = entity.tpe
		val table = tpe.table
		val lazyLoad = selectConfig.lazyLoad

		val lazyLoadedMods = (table.columnInfosPlain.map {
			ci =>
				val ll = lazyLoad.isLazyLoaded(ci)
				ci match {
					case mtm: ColumnInfoTraversableManyToMany[_, _, _] =>
						(ci.column.alias, if (ll) Nil else vm.valueOf(ci))
					case mto: ColumnInfoManyToOne[_, _, _] =>
						(ci.column.alias, if (ll) null else vm.valueOf(ci))
					case mtm: ColumnInfoTraversableOneToMany[_, _, _, _] =>
						(ci.column.alias, if (ll) Nil else vm.valueOf(ci))
					case otor: ColumnInfoOneToOneReverse[_, _, _] =>
						(ci.column.alias, if (ll) null else vm.valueOf(ci))
					case _ => (ci.column.alias, vm.valueOf(ci))
				}
		} ::: table.extraColumnInfosPersisted.map {
			ci =>
				(ci.column.alias, vm.valueOf(ci))
		}).toMap
		val lazyLoadedVM = ValuesMap.fromMap(null, lazyLoadedMods)
		val constructed = tpe.constructor(selectConfig.data, lazyLoadedVM)
		val proxy = lazyLoadManager.proxyFor(constructed, entity, lazyLoad, vm)
		lazyLoadedVM.o = proxy
		proxy
	}

	/**
	 * create a mock of the current entity, to avoid cyclic dependencies
	 * doing infinite loops.
	 */

	override def delete[ID, PC <: Persisted, T](entity: Entity[ID, PC, T], id: ID) {
		val ids = Helpers.idToList(id)
		val tpe = entity.tpe
		val table = tpe.table
		val pks = table.primaryKeys
		if (pks.size != ids.size) throw new IllegalArgumentException("number of primary key values don't match number of primary keys : %s != %s".format(pks, ids))
		val keyValues = pks zip ids
		// do the actual delete database op
		driver.doDelete(tpe, keyValues)
	}

	/**
	 * deletes an entity from the database
	 */
	override def delete[ID, PC <: Persisted, T](deleteConfig: DeleteConfig, entity: Entity[ID, PC, T], o: T with PC): T = {
		val entityMap = new UpdateEntityMap
		val deleted = deleteInner(deleteConfig, entity, o, entityMap)
		entityMap.done()
		deleted
	}

	private[mapperdao] def deleteInner[ID, T](
		deleteConfig: DeleteConfig,
		entity: Entity[ID, Persisted, T],
		o: T with Persisted,
		entityMap: UpdateEntityMap
		): T = {
		if (o.mapperDaoDiscarded) throw new IllegalArgumentException("can't operate on an object twice. An object that was updated/deleted must be discarded and replaced by the return value of update(), i.e. onew=update(o) or just be disposed if it was deleted. The offending object was : " + o)

		val tpe = entity.tpe
		val table = tpe.table

		try {
			val keyValues0 = table.toListOfPrimaryKeySimpleColumnAndValueTuples(o) ::: beforeDeletePlugins.flatMap(
				_.idColumnValueContribution(tpe, deleteConfig, o, entityMap)
			)

			val keyValues = keyValues0 ::: table.toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(o)
			// call all the before-delete plugins
			beforeDeletePlugins.foreach {
				_.before(entity, deleteConfig, o, keyValues, entityMap)
			}

			// do the actual delete database op
			driver.doDelete(tpe, keyValues)

			// return the object
			o
		} catch {
			case e: Throwable => throw new PersistException("An error occured during delete of entity %s with value %s".format(entity, o), e :: Nil)
		}
	}

	override def unlink[ID, PC <: Persisted, T](entity: Entity[ID, PC, T], o: T): T = {
		val unlinkVisitor = new UnlinkEntityRelationshipVisitor
		unlinkVisitor.visit(entity, o)
		unlinkVisitor.unlink(o)
		o
	}

	override def link0[ID, T](entity: Entity[ID, Persisted, T], o: T with Persisted) = {
		val vm = ValuesMap.fromType(typeManager, entity.tpe, o)
		val r = entity.constructor(None, vm)
		r.mapperDaoValuesMap = vm
		r
	}

	override def merge0[ID, T](
		selectConfig: SelectConfig,
		updateConfig: UpdateConfig,
		entity: Entity[ID, Persisted, T],
		o: T,
		ids: ID
		): T with Persisted =
		select(selectConfig, entity, ids) match {
			case None => insert(updateConfig, entity, o)
			case Some(oldO) =>
				update(updateConfig, entity, oldO, o)
		}

	/**
	 * ===================================================================================
	 * common methods
	 * ===================================================================================
	 */
	override def toString = "MapperDao(%s)".format(driver)
}
