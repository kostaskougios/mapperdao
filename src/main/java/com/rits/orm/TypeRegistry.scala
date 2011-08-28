package com.rits.orm
import java.util.IdentityHashMap

/**
 * a registry of entities
 *
 * @author kostantinos.kougios
 *
 * 25 Jul 2011
 */
final class TypeRegistry(entities: List[Entity[_, _]]) {
	private var types: Map[Class[_], Entity[_, _]] = _
	private val columnsToEntity = new IdentityHashMap[ColumnBase, Entity[_, _]]
	private var entityToType: Map[Entity[_, _], Type[_, _]] = _

	{
		var typesBuilder = Map.newBuilder[Class[_], Entity[_, _]]
		var entityToTypeBuilder = Map.newBuilder[Entity[_, _], Type[_, _]]

		entities.foreach { entity =>
			def create[PC, T]: Type[PC, T] = Type(entity.clz.asInstanceOf[Class[T]], entity.constructor.asInstanceOf[(com.rits.orm.ValuesMap) => T with PC with com.rits.orm.Persisted], Table[PC, T](entity.table, entity.columns.reverse.asInstanceOf[List[com.rits.orm.ColumnInfoBase[T, _]]], entity.persistedColumns.asInstanceOf[List[com.rits.orm.ColumnInfoBase[T with PC, _]]]))
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
	def typeOf[PC, T](entity: Entity[PC, T]): Type[PC, T] = entityToType(entity).asInstanceOf[Type[PC, T]]
	def typeOf[PC, T](clz: Class[T]): Type[PC, T] = entityToType(entityOf(clz)).asInstanceOf[Type[PC, T]]
	def typeOf[PC, T](column: ColumnBase): Type[PC, T] = entityToType(entityOf(column)).asInstanceOf[Type[PC, T]]
	def typeOf[PC, T](o: T): Type[PC, T] = entityToType(entityOf(o)).asInstanceOf[Type[PC, T]]

	def entityOf(column: ColumnBase): Entity[_, _] = {
		val e = columnsToEntity.get(column)
		if (e == null) throw new IllegalArgumentException("can't find entity for column %s, is entity registered with this type registry?".format(column))
		e
	}

	/**
	 * utility methods
	 */
	protected[orm] def entityOf[PC, T](clz: Class[T]): Entity[PC, T] =
		{
			types(clz).asInstanceOf[Entity[PC, T]]
		}

	protected[orm] def entityOf[PC, T](o: T): Entity[PC, T] =
		{
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
	def apply(types: Entity[_, _]*): TypeRegistry = new TypeRegistry(types.toList)
	def apply(types: List[Entity[_, _]]): TypeRegistry = new TypeRegistry(types)
}