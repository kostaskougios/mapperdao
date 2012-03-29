package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.CacheOption

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
	 * if the key exists in the cache, then the cached value is returned according
	 * to CacheOption.
	 * Otherwise valueCalculator() should be invoked which will calculate the
	 * value. The calculated value is then cached according to the CacheOption
	 */
	def apply[T](key: List[Any], options: CacheOption)(valueCalculator: => T): T

	/**
	 * just cache the key/value pair
	 */
	def put[T](key: List[Any], t: T)
}