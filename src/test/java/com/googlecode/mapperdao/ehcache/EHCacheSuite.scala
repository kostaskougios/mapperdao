package com.googlecode.mapperdao.ehcache

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import net.sf.ehcache.CacheManager
import com.googlecode.mapperdao.CacheOptions
import scala.actors.Actor._

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class EHCacheSuite extends FunSuite with ShouldMatchers {

	test("multithreaded accessing same key") {
		val cacheManager = CacheManager.create
		try {
			val ehCache = cacheManager.getCache("test")
			val cache = new CacheUsingEHCache(ehCache)

			def createActor = actor {
				loop {
					react {
						case -1 =>
							sender ! 'Ok
							exit()
						case iteration: Int =>
							//println(iteration + ":" + Thread.currentThread.getName)
							cache(List("key1", 1), CacheOptions.OneDay) {
								10
							}
					}
				}
			}.start

			val a1 = createActor
			val a2 = createActor
			val a3 = createActor
			for (i <- 1 to 10000) {
				a1 ! i
				a2 ! i
				a3 ! i
			}
			a1 !? -1
			a2 !? -1
			a3 !? -1
		} finally {
			cacheManager.shutdown()
		}
	}

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