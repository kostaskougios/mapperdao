package com.googlecode.mapperdao

/**
 * a Type holds type information for an entity
 *
 * this is internal mapperdao API
 *
 * @author kostantinos.kougios
 */
case class Type[ID, T](
	val clz: Class[T],
	val constructor: (Option[_], ValuesMap) => T with DeclaredIds[ID],
	table: Table[ID, T]
) {

	override def equals(o: Any) = o match {
		case t: Type[_, _] => t.clz.equals(clz) && t.table.name.equals(table.name)
		case _ => false
	}

	override def hashCode = table.name.hashCode
}
