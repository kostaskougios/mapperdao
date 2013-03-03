package com.googlecode.mapperdao

import java.util.IdentityHashMap

/**
 * a registry of entities
 *
 * @author kostantinos.kougios
 *
 *         25 Jul 2011
 */
final class TypeRegistry private(val entities: List[Entity[_,_, _]]) {
	private val columnsToEntity = new IdentityHashMap[ColumnBase, Entity[Any,_, Any]]

	entities.foreach {
		entity =>
			entity.init
			val columns = entity.onlyForQueryColumns.map {
				ci =>
					ci.column
			} ::: entity.tpe.table.columns
			columns.foreach {
				c =>
					columnsToEntity.put(c, entity.asInstanceOf[Entity[Any,_, Any]])
			}
	}

	def entityOf(column: ColumnBase): Entity[Any,_, Any] = {
		val e = columnsToEntity.get(column)
		if (e == null)
			throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	override def toString = "TypeRegistry(%s)".format(entities)
}

object TypeRegistry {
	/**
	 * creates a TypeRegistry, registers all types and initializes the TypeRegistry.
	 */
	def apply(types: Entity[_,_, _]*): TypeRegistry = new TypeRegistry(types.toList)

	def apply(types: List[Entity[_,_, _]]): TypeRegistry = new TypeRegistry(types)
}