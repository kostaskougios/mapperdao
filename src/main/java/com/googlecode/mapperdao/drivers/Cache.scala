package com.googlecode.mapperdao.drivers

/**
 * implement this to provide caching for mapperdao drivers. The implementation must
 * be thread safe.
 *
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait Cache {
	/**
	 * if the key exists in the cache, then the cached value is returned.
	 * Otherwise valueCalculator() should be invoked which will calculate the
	 * value. The calculated value is then cached for expireInMs milliseconds.
	 */
	def apply[T](key: List[Any], expireInMs: Long)(valueCalculator: => T): T
}