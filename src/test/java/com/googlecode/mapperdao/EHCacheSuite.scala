package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import net.sf.ehcache.CacheManager

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class EHCacheSuite extends FunSuite with ShouldMatchers {
	test("cache") {
		val cacheManager = CacheManager.create
		val cache = cacheManager.getCache("test")
		val key = List("key", 1)
		cache.acquireReadLockOnKey(key)
		cache.releaseReadLockOnKey(key)
	}
}