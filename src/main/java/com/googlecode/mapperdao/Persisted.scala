package com.googlecode.mapperdao

/**
 * Every persisted object must mixin with this trait.
 *
 * @author kostantinos.kougios
 *
 * 17 Jul 2011
 */
trait Persisted {
	val valuesMap: ValuesMap

	// after an orm operation, an object must be discarded. This guards the rule.
	protected[mapperdao] var discarded = false

	// mock objects are used in object graphs with cycles
	protected[mapperdao] var mock = false
}