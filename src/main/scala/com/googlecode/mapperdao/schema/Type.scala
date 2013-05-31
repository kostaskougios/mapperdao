package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.{Persisted, ValuesMap}
import com.googlecode.mapperdao.internal.PersistedDetails

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
	val constructor: (PersistedDetails, Option[_], ValuesMap) => T with Persisted
	val table: Table[ID, T]
}


