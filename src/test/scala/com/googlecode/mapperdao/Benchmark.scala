package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup

/**
 * benchmark : an attempt to isolate and benchmark mapperdao
 *
 * @author kostantinos.kougios
 *
 * 9 Oct 2012
 */
object Benchmark extends App {
	import CommonEntities._
	val dataSource = Setup.singleConnectionDataSource
	val (jdbc, mapperDao, queryDao, txManager) = Setup.from(dataSource, List(ProductEntity, AttributeEntity))

	val p = Product(
		"test product",
		Set(
			Attribute("colour", "red"),
			Attribute("colour", "green"),
			Attribute("colour", "blue")
		)
	)

	val loops = args(0).toInt
	println("will run for %d loops".format(loops))
	createProductAttribute(jdbc)

	val method = args(1) match {
		case "insert" => benchmarkInsert _
		case "select" =>
			val inserted = mapperDao.insert(ProductEntity, p)
			benchmarkSelect(inserted.id, _: Int)
	}

	println("warm up...")
	method(500)

	//	println("press enter for the test to start")
	//	readLine

	println("benchmarking...")
	val start = System.currentTimeMillis
	method(loops)
	val stop = System.currentTimeMillis
	println("dt : " + (stop - start))

	def benchmarkInsert(loops: Int) {
		for (i <- 0 to loops) {
			mapperDao.insert(ProductEntity, p)
		}
	}

	def benchmarkSelect(id: Int, loops: Int) {
		for (i <- 0 to loops) {
			mapperDao.select(ProductEntity, id)
		}
	}
}