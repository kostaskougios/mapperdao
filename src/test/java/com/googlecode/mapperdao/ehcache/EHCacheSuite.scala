package com.googlecode.mapperdao.ehcache

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import net.sf.ehcache.CacheManager
import com.googlecode.mapperdao.CacheOptions

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class EHCacheSuite extends FunSuite with ShouldMatchers {
	test("cache positive") {
		val cacheManager = CacheManager.create
		try {
			val ehCache = cacheManager.getCache("test")
			val cache = new CacheUsingEHCache(ehCache) with Locking

			var calculated = 0

			cache(List("key1", 1), CacheOptions.OneDay) {
				calculated += 1
				10
			} should be(10)
			calculated should be(1)

			cache(List("key1", 1), CacheOptions.OneDay) {
				calculated += 1
				10
			} should be(10)
			calculated should be(1)
		} finally {
			cacheManager.shutdown()
		}
	}
	test("cache negative") {
		val cacheManager = CacheManager.create
		try {
			val ehCache = cacheManager.getCache("test")
			val cache = new CacheUsingEHCache(ehCache) with Locking

			var calculated = 0

			cache(List("key1", 1), CacheOptions.OneDay) {
				calculated += 1
				10
			} should be(10)
			calculated should be(1)

			cache(List("key2", 2), CacheOptions.OneDay) {
				calculated += 1
				50
			} should be(50)
			calculated should be(2)
		} finally {
			cacheManager.shutdown()
		}
	}
}