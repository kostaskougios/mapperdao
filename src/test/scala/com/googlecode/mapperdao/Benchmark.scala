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

	println("warm up...")
	val method = args(1) match {
		case "insert" =>
			val m = benchmarkInsert _
			m(500)
			m
		case "select" =>
			val inserted = mapperDao.insert(ProductEntity, p)
			val m = benchmarkSelect(inserted.id, _: Int)
			m(500)
			m
		case "update" =>
			benchmarkUpdate(mapperDao.insert(ProductEntity, p), 500)
			benchmarkUpdate(mapperDao.insert(ProductEntity, p), _: Int)
	}

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

	def benchmarkUpdate(inserted: Product with SurrogateIntId, loops: Int) {
		var old = inserted
		for (i <- 0 to loops) {
			old = mapperDao.update(ProductEntity, old, p)
		}
	}
}