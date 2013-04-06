package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.{Type, Table}

/**
 * @author: kostas.kougios
 *          Date: 03/01/13
 */
protected case class EntityType[ID, T](
	clz: Class[T],
	constructor: (Option[_], ValuesMap) => T with Persisted,
	table: Table[ID, T]
	) extends Type[ID, T]
{
	override def equals(o: Any) = o match {
		case t: Type[_, _] => t.clz.equals(clz) && t.table.name.equals(table.name)
		case _ => false
	}

	override def hashCode = table.name.hashCode

	override def toString = "EntityType(" + clz.getSimpleName + ")"
}
