package com.rits.orm
import java.util.IdentityHashMap

/**
 * a registry of entities
 *
 * @author kostantinos.kougios
 *
 * 25 Jul 2011
 */
final class TypeRegistry {
	private var typesBuilder = Map.newBuilder[Class[_], Entity[_, _]]
	private var types: Map[Class[_], Entity[_, _]] = _
	private val columnsToEntity = new IdentityHashMap[ColumnBase, Entity[_, _]]

	/**
	 * registration of entities
	 */
	def registerType[PC, T](entity: Entity[PC, T]): Unit =
		{
			if (types != null) throw new IllegalStateException("already initialized, cant register types after initialization")
			// initialize entity
			entity.init

			entity.tpe.table.columns.foreach { c =>
				columnsToEntity.put(c, entity)
			}
			typesBuilder += entity.clz -> entity
		}

	def entityOf(column: ColumnBase): Entity[_, _] = {
		val e = columnsToEntity.get(column)
		if (e == null) throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	private def checkInit(): Unit =
		{
			if (types == null) throw new IllegalStateException("Not yet initialized. Please call init method after registering all types")
		}
	/**
	 * call this after registering all types
	 */
	def init: Unit =
		{
			if (types != null) throw new IllegalStateException("already initialized. Please init only once after registering all types")
			types = typesBuilder.result
			typesBuilder = null
		}

	/**
	 * utility methods
	 */
	protected[orm] def entityOf[PC, T](clz: Class[T]): Entity[PC, T] =
		{
			checkInit
			types(clz).asInstanceOf[Entity[PC, T]]
		}

	protected[orm] def typeInfo[PC, T](o: T): Entity[PC, T] =
		{
			checkInit
			// there is a very weird compilation error from maven
			// if I just do val clz = o.getClass.asInstanceOf[Class[T]]
			val clz = o.asInstanceOf[Object].getClass.asInstanceOf[Class[T]]
			o match {
				case p: Persisted => types.get(clz.getSuperclass).get.asInstanceOf[Entity[PC, T]]
				case _ => entityOf(clz)
			}
		}
}

object TypeRegistry {
	/**
	 * creates a TypeRegistry, registers all types and initializes the TypeRegistry.
	 */
	def apply(types: Entity[_, _]*): TypeRegistry =
		{
			val registry = new TypeRegistry
			types.foreach { t =>
				registry.registerType(t.asInstanceOf[Entity[Any, Any]])
			}
			registry.init
			registry
		}
}