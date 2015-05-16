package com.googlecode.mapperdao.ehcache

import com.googlecode.mapperdao.CacheOptions
import com.googlecode.mapperdao.jdbc.Setup
import net.sf.ehcache.CacheManager
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
/**
 * @author kostantinos.kougios
 *
 *         24 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class EHCacheSuite extends FunSuite with BeforeAndAfter
{
	if (Setup.database == "h2") {
		val cacheManager = CacheManager.create
		val ehCache = cacheManager.getCache("EHCacheSuite")
		test("cache expires positive") {
			val cache = new CacheUsingEHCache(ehCache) with Locking

			var calculated = 0

			val co = CacheOptions(10)
			cache(List("key1", 1), co) {
				calculated += 1
				10
			} should be(10)
			calculated should be(1)

			Thread.sleep(30)
			cache(List("key1", 1), co) {
				calculated += 1
				20
			} should be(20)
			calculated should be(2)
		}

		test("cache positive") {
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
		}

		test("cache negative") {
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
		}

		before {
			ehCache.flush()
		}
	}
}