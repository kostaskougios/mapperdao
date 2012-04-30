package com.googlecode.mapperdao

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

	// default configurations, can be overriden
	protected val defaultSelectConfig = SelectConfig.default
	protected val defaultDeleteConfig = DeleteConfig.default
	protected val defaultUpdateConfig = UpdateConfig(deleteConfig = defaultDeleteConfig)

	// delete

	/**
	 * deletes an entity from the database. By default, related entities won't be deleted, please use
	 * delete(deleteConfig, entity, o) to fine tune the operation
	 */
	def delete[PC, T](entity: Entity[PC, T], o: T with PC): T = delete(defaultDeleteConfig, entity, o)
	def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T

	/**
	 * this will delete an entity based on it's id.
	 *
	 * The delete will cascade to related entities only if there are cascade constraints
	 * on the foreign keys in the database. In order to configure mapperdao to delete
	 * related entities, select() the entity first and then delete it using
	 * delete(deleteConfig, entity, o). (In any case to do the same at the database level,
	 * queries would be required in order to delete the related data)
	 */
	def delete[PC, T](entity: Entity[PC, T], id: AnyVal): Unit = delete(entity, List(id))
	def delete[PC, T](entity: Entity[PC, T], ids: List[AnyVal]): Unit

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
}
