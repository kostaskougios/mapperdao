package com.googlecode.mapperdao.stresstesting

import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.CommonEntities
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.LazyLoad
import com.googlecode.mapperdao.ehcache.CacheUsingEHCache
import com.googlecode.mapperdao.ehcache.Locking
import net.sf.ehcache.CacheManager

/**
 * @author kostantinos.kougios
 *
 * 27 May 2012
 */
object StressTestCPUUsage extends App {
	import CommonEntities._

	val cache = if (args(1).contains("cached")) {
		println("With Cache")
		val cacheManager = CacheManager.create
		val ehCache = cacheManager.getCache("StressTestCPUUsage")
		Some(new CacheUsingEHCache(ehCache) with Locking)
	} else None
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(
		TypeRegistry(ProductEntity, AttributeEntity),
		cache = cache)

	Setup.dropAllTables(jdbc)
	Setup.commonEntitiesQueries(jdbc).update("product-attribute")

	val data1 = mapperDao.insert(ProductEntity, Product("test", Set(Attribute("colour", "red"), Attribute("colour", "green"))))

	val (f, loops) = args(0) match {
		case "select" => (select, 500000)
		case "lazySelect" => (lazySelect, 500000)
	}

	println("warm up")
	for (i <- 0 to 5000) {
		f()
	}

	println("benchmark")
	val start = System.currentTimeMillis
	for (i <- 0 to loops) {
		f()
	}
	val dt = System.currentTimeMillis - start
	println("Dt		: 	%d millis".format(dt))
	println("throughput	:	%d per sec".format((1000 * loops) / dt))

	def select = () => mapperDao.select(ProductEntity, data1.id)
	def lazySelect = () => mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, data1.id)
}