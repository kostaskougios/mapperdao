package com.googlecode.mapperdao

/**
 * a Type holds type information for an entity
 *
 * this is internal mapperdao API
 *
 * @author kostantinos.kougios
 */
case class Type[ID, PC <: DeclaredIds[ID], T](val clz: Class[T], val constructor: (Option[_], ValuesMap) => T with PC, table: Table[ID, PC, T])
