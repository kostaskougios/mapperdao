package com.googlecode.mapperdao.ehcache

import scala.actors.Actor.actor
import scala.actors.Actor.exit
import scala.actors.Actor.loop
import scala.actors.Actor.react
import scala.actors.Actor.sender
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.googlecode.mapperdao.CacheOptions
import net.sf.ehcache.CacheManager
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfter
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class EHCacheSuite extends FunSuite with ShouldMatchers with BeforeAndAfter {
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

		test("multithreaded accessing same key") {
			val cache = new CacheUsingEHCache(ehCache) with Locking

			def createActor = actor {
				var i = 0
				var errors = 0
				loop {
					react {
						case iteration: Int =>
							//println(iteration + ":" + Thread.currentThread.getName)
							val key = List("key1", iteration)
							if (cache(key, CacheOptions.OneDay) {
								// check if locking works, so no key is calculated twice
								if (ehCache.get(key) != null) {
									errors += 1
									//println("Locking ERROR!")
								}
								iteration
							} != iteration) errors += 1 // check if we get the correct result
							i += 1
							if (i % 10 == 0) ehCache.remove(key)
						case 'exit =>
							sender ! errors
							exit()
					}
				}
			}.start

			val actors = for (i <- 1 to 100) yield createActor
			for (i <- 1 to 10000) {
				for (actor <- actors) actor ! i % 10
			}
			for (actor <- actors)
				(actor !? 'exit) should be(0)
		}

		before {
			ehCache.flush()
		}
	}
}