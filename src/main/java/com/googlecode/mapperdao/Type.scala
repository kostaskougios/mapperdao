package com.googlecode.mapperdao

/**
 * a Type holds type information for an entity
 *
 * this is internal mapperdao API
 *
 * @author kostantinos.kougios
 */
case class Type[PC, T](val clz: Class[T], val constructor: (Option[_], ValuesMap) => T with PC with Persisted, table: Table[PC, T])
