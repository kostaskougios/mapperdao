package com.googlecode.mapperdao
import java.util.IdentityHashMap

/**
 * a registry of entities
 *
 * @author kostantinos.kougios
 *
 * 25 Jul 2011
 */
final class TypeRegistry private (entities: List[Entity[_, _]]) {
	private val columnsToEntity = new IdentityHashMap[ColumnBase, Entity[_, _]]

	entities.foreach { entity =>
		entity.tpe.table.columns.foreach { c =>
			columnsToEntity.put(c, entity)
		}
	}

	def entityOf(column: ColumnBase): Entity[_, _] = {
		val e = columnsToEntity.get(column)
		if (e == null) throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	override def toString = "TypeRegistry(%s)".format(entities)
}

object TypeRegistry {
	/**
	 * creates a TypeRegistry, registers all types and initializes the TypeRegistry.
	 */
	def apply(types: Entity[_, _]*): TypeRegistry = new TypeRegistry(types.toList)
	def apply(types: List[Entity[_, _]]): TypeRegistry = new TypeRegistry(types)
}