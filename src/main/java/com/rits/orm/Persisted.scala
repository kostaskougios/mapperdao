package com.rits.orm

/**
 * @author kostantinos.kougios
 *
 * 17 Jul 2011
 */
trait Persisted {
	val valuesMap: ValuesMap

	// after an orm operation, an object must be discarded. This guards the rule.
	protected[orm] var discarded = false
	protected[orm] var mock = false
}