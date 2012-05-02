package com.googlecode.mapperdao

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

	def delete[PC, T](entity: Entity[PC, T], ids: List[AnyVal]): Unit = {}

	override def link[T](entity: SimpleEntity[T], o: T): T = throw new IllegalStateException("Not supported")
	override def link[T](entity: Entity[IntId, T], o: T, id: Int): T with IntId = throw new IllegalStateException("Not supported")
}