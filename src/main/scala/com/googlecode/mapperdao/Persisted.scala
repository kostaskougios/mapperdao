package com.googlecode.mapperdao

/**
 * This is considered mapperdao internal implementation. It is not
 * needed to mixin this trait in the constructor() method anymore
 *
 * @author kostantinos.kougios
 *
 * 17 Jul 2011
 */
private[mapperdao] trait Persisted {

	// to avoid naming conflicts, all these field names start with "mapperDao" 
	@transient
	private[mapperdao] var mapperDaoValuesMap: ValuesMap = null

	// after an orm operation, an object must be discarded. This guards the rule.
	@transient
	private[mapperdao] var mapperDaoDiscarded = false

	// mock objects are used in object graphs with cycles
	@transient
	private[mapperdao] var mapperDaoMock = false
}