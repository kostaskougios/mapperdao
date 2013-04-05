package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.Table

/**
 * a Type holds type information for an entity
 *
 * this is internal mapperdao API
 *
 * @author kostantinos.kougios
 */
trait Type[ID, T]
{
	val clz: Class[T]
	val constructor: (Option[_], ValuesMap) => T with Persisted
	val table: Table[ID, T]
}


