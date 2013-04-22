package com.googlecode.mapperdao

/**
 * This is considered mapperdao internal implementation.
 *
 * @author kostantinos.kougios
 *
 *         17 Jul 2011
 */
trait Persisted
{

	// to avoid naming conflicts, all these field names start with "mapperDao" 
	@transient
	private[mapperdao] var mapperDaoValuesMap: ValuesMap = null

	// after an orm operation, an object must be discarded. This guards the rule.
	@transient
	private[mapperdao] var mapperDaoDiscarded = false

	@transient
	private var mapperDaoReplacedBy: Option[Any] = None

	private[mapperdao] def mapperDaoReplaced_=(v: Any) {
		if (v == null) throw new NullPointerException("can't replace entity with null")
		if (mapperDaoReplacedBy.isDefined) throw new IllegalStateException("entity was already replaced by " + mapperDaoReplacedBy)
		mapperDaoReplacedBy = Some(v)
	}

	private[mapperdao] def mapperDaoReplaced = mapperDaoReplacedBy
}