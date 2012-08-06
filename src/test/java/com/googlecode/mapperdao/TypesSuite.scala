package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 6 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class TypesSuite extends FunSuite with ShouldMatchers {
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry())

	test("bigdecimal") {
		createTables("bigdecimal")
		val big = BigDecimal(500, 5)
		val inserted = mapperDao.insert(BDEntity, BD(5, big))
		inserted should be === BD(5, big)
		mapperDao.select(BDEntity, 5).get should be === BD(5, big)
	}

	def createTables(ddl: String) = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update(ddl)
	}

	case class BD(id: Int, big: BigDecimal)

	object BDEntity extends SimpleEntity[BD] {
		val id = key("id") to (_.id)
		val big = column("big") to (_.big)

		def constructor(implicit m) = new BD(id, big) with Persisted
	}
}