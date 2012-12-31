package com.googlecode.mapperdao

/**
 * all id traits should extend this one
 *
 * @author kostantinos.kougios
 *
 *         24 Sep 2012
 */
trait DeclaredIds[ID] extends Persisted {
	val id: ID
}