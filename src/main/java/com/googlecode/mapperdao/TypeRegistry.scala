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

	{
		entities.foreach { entity =>
			def create[PC, T]: Type[PC, T] = {
				val constructor: (ValuesMap) => T with PC with Persisted = m => {
					// construct the object
					val o = entity.constructor(m).asInstanceOf[T with PC with Persisted]
					// set the values map
					o.valuesMap = m
					o
				}
				Type(entity.clz.asInstanceOf[Class[T]], constructor, Table[PC, T](entity.table, entity.columns.reverse.asInstanceOf[List[ColumnInfoBase[T, _]]], entity.persistedColumns.asInstanceOf[List[ColumnInfoBase[T with PC, _]]], entity.unusedPKs))
			}
			val tpe = create[Any, Any]
			entity.init(tpe)
			tpe.table.columns.foreach { c =>
				columnsToEntity.put(c, entity)
			}
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