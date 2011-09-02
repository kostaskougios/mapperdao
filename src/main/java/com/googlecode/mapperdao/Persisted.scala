package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 17 Jul 2011
 */
trait Persisted {
	val valuesMap: ValuesMap

	// after an orm operation, an object must be discarded. This guards the rule.
	protected[mapperdao] var discarded = false
	protected[mapperdao] var mock = false
}