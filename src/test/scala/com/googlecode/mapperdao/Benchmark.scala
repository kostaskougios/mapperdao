package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 9 Oct 2012
 */
object Benchmark extends App {
	import CommonEntities._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	val p = Product(
		"test product",
		Set(
			Attribute("colour", "red"),
			Attribute("colour", "green"),
			Attribute("colour", "blue")
		)
	)

	createProductAttribute(jdbc)

	println("warm up...")
	benchmarkInsert(500)

	println("benchmarking...")
	val start = System.currentTimeMillis
	benchmarkInsert(10000)
	val stop = System.currentTimeMillis
	println("dt : " + (stop - start))

	def benchmarkInsert(loops: Int) {
		for (i <- 0 to loops) {
			mapperDao.insert(ProductEntity, p)
		}
	}
}