package com.googlecode.mapperdao.stresstesting

import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.CommonEntities
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 27 May 2012
 */
object StressTestCPUUsage extends App {
	import CommonEntities._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))
	Setup.dropAllTables(jdbc)
	Setup.commonEntitiesQueries(jdbc).update("product-attribute")

	val start = System.currentTimeMillis
	args(0) match {
		case "select" => select()
	}
	val dt = System.currentTimeMillis - start

	println("Dt : %d millis".format(dt))

	def select() {
		for (i <- 0 to 100000) {
			mapperDao.select(ProductEntity, i)
		}
	}

}