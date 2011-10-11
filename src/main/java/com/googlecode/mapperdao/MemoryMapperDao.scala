package com.googlecode.mapperdao
import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * a memory implementation of the MapperDao interface, useful for testing
 * or prototype creation
 *
 * @author kostantinos.kougios
 *
 * 11 Oct 2011
 */
class MemoryMapperDao extends MapperDao {
	// insert
	def insert[PC, T](entity: Entity[PC, T], o: T): T with PC = null.asInstanceOf[T with PC]

	// update
	def update[PC, T](entity: Entity[PC, T], o: T with PC): T with PC = null.asInstanceOf[T with PC]
	// update immutable
	def update[PC, T](entity: Entity[PC, T], o: T with PC, newO: T): T with PC = null.asInstanceOf[T with PC]

	// select
	def select[PC, T](selectConfig: SelectConfig, entity: Entity[PC, T], ids: List[Any]): Option[T with PC] = None

	// delete
	def delete[PC, T](deleteConfig: DeleteConfig, entity: Entity[PC, T], o: T with PC): T = null.asInstanceOf[T]

	// used internally
	private[mapperdao] def toEntities[PC, T](lm: List[JdbcMap], tpe: Type[PC, T], selectConfig: SelectConfig, entities: EntityMap): List[T with PC] = throw new RuntimeException()
}