package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup

/**
 * benchmark : an attempt to isolate and benchmark mapperdao
 *
 * @author kostantinos.kougios
 *
 *         9 Oct 2012
 */
object Benchmark extends App
{

	import CommonEntities._

	val dataSource = Setup.singleConnectionDataSource
	val (jdbc, mapperDao, queryDao, txManager) = Setup.from(dataSource, List(ProductEntity, AttributeEntity))

	def p = Product(
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

	println("warm up " + args(1))
	val method = args(1) match {
		case "insert" =>
			val m = benchmarkInsert _
			for (i <- 0 to 50) m(5)
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

	/**
	 * History:
	 *
	 * date			loops			dt
	 * ------------------------------------
	 * 2012/02/05	1000			5116
	 * 2012/02/05	10000			32800
	 * before		1000			11500
	 * before		10000			81300
	 *
	 */
	def benchmarkInsert(loops: Int) {
		val l = (for (i <- 1 to loops) yield p).toList
		mapperDao.insertBatch(UpdateConfig.default, ProductEntity, l)
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