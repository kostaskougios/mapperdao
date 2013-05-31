package com.googlecode.mapperdao

import java.util.IdentityHashMap
import com.googlecode.mapperdao.schema.{Type, ColumnBase}
import com.googlecode.mapperdao.internal.PersistedDetails

/**
 * a registry of entities
 *
 * @author kostantinos.kougios
 *
 *         25 Jul 2011
 */
final class TypeRegistry private(typeManager: TypeManager, val entities: List[EntityBase[_, _]])
{
	private val columnsToEntity = new IdentityHashMap[ColumnBase, EntityBase[Any, Any]]
	private val persistedDetailsPerTpe = entities.map {
		e =>
			(e.tpe, new PersistedDetails(e, typeManager))
	}.toMap

	entities.foreach {
		entity =>
			entity match {
				case e: Entity[_, Persisted, _] =>
					e.init()
					val columns = e.onlyForQueryColumns.map {
						ci =>
							ci.column
					} ::: entity.tpe.table.columns
					columns.foreach {
						c =>
							columnsToEntity.put(c, entity.asInstanceOf[EntityBase[Any, Any]])
					}
				case _ => // noop
			}
	}

	def entityOf(column: ColumnBase): EntityBase[Any, Any] = {
		val e = columnsToEntity.get(column)
		if (e == null)
			throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	def persistDetails(tpe: Type[_, _]) = persistedDetailsPerTpe(tpe)

	override def toString = "TypeRegistry(%s)".format(entities)
}

object TypeRegistry
{
	/**
	 * creates a TypeRegistry, registers all types and initializes the TypeRegistry.
	 */
	def apply(typeManager: TypeManager, types: EntityBase[_, _]*): TypeRegistry = new TypeRegistry(typeManager, types.toList)

	def apply(typeManager: TypeManager, types: List[EntityBase[_, _]]): TypeRegistry = new TypeRegistry(typeManager, types)
}