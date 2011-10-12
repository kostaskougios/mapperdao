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
	private var types: Map[Class[_], Entity[_, _]] = _
	private val columnsToEntity = new IdentityHashMap[ColumnBase, Entity[_, _]]
	private var entityToType: Map[Entity[_, _], Type[_, _]] = _

	{
		var typesBuilder = Map.newBuilder[Class[_], Entity[_, _]]
		var entityToTypeBuilder = Map.newBuilder[Entity[_, _], Type[_, _]]

		entities.foreach { entity =>
				def create[PC, T]: Type[PC, T] = {
					val constructor: (ValuesMap) => T with PC with Persisted = m => {
						// construct the object
						val o = entity.constructor(m).asInstanceOf[T with PC with Persisted]
						// set the values map
						o.valuesMap = m
						o
					}
					Type(entity.clz.asInstanceOf[Class[T]], constructor, Table[PC, T](entity.table, entity.columns.reverse.asInstanceOf[List[ColumnInfoBase[T, _]]], entity.persistedColumns.asInstanceOf[List[ColumnInfoBase[T with PC, _]]]))
				}
			val tpe = create[Any, Any]
			tpe.table.columns.foreach { c =>
				columnsToEntity.put(c, entity)
			}
			typesBuilder += entity.clz -> entity
			entityToTypeBuilder += entity -> tpe
		}
		types = typesBuilder.result
		entityToType = entityToTypeBuilder.result
	}

	def typeOf[PC, T](entity: Entity[PC, T]): Type[PC, T] =
		{
			val e = entityToType.getOrElse(entity, null)
			if (e == null) throw new IllegalStateException("can't find entity's type, is entity %s registered?".format(entity))
			e.asInstanceOf[Type[PC, T]]
		}
	def typeOf[PC, T](clz: Class[T]): Type[PC, T] = entityToType(entityOf(clz)).asInstanceOf[Type[PC, T]]
	def typeOf[PC, T](column: ColumnBase): Type[PC, T] = entityToType(entityOf(column)).asInstanceOf[Type[PC, T]]
	def typeOfObject[PC, T](o: T): Type[PC, T] = {
		if (o == null) throw new NullPointerException("can't find type of null object")
		entityToType(entityOfObject(o)).asInstanceOf[Type[PC, T]]
	}

	def entityOf(column: ColumnBase): Entity[_, _] = {
		val e = columnsToEntity.get(column)
		if (e == null) throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	/**
	 * utility methods
	 */
	protected[mapperdao] def entityOf[PC, T](clz: Class[T]): Entity[PC, T] =
		{
			val entity = types.getOrElse(clz, types.getOrElse(clz.getSuperclass(), null))
			if (entity == null) throw new IllegalStateException("entity not registered for " + clz)
			entity.asInstanceOf[Entity[PC, T]]
		}

	protected[mapperdao] def entityOfObject[PC, T](o: T): Entity[PC, T] =
		{
			if (o == null) throw new NullPointerException("can't locate the entity of a null object")
			// there is a very weird compilation error from maven
			// if I just do val clz = o.getClass.asInstanceOf[Class[T]]
			val clz = o.asInstanceOf[Object].getClass.asInstanceOf[Class[T]]
			o match {
				case p: Persisted => entityOf(clz.getSuperclass).asInstanceOf[Entity[PC, T]]
				case _ => entityOf(clz)
			}
		}

	override def toString = "TypeRegistry(%s)".format(types)
}

object TypeRegistry {
	/**
	 * creates a TypeRegistry, registers all types and initializes the TypeRegistry.
	 */
	def apply(types: Entity[_, _]*): TypeRegistry = new TypeRegistry(types.toList)
	def apply(types: List[Entity[_, _]]): TypeRegistry = new TypeRegistry(types)
}