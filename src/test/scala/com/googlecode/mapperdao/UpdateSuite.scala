package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 29 Oct 2012
 */
@RunWith(classOf[JUnitRunner])
class UpdateSuite extends FunSuite with ShouldMatchers {
	import CommonEntities._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	test("update simple") {
		createProductAttribute(jdbc)
		val (p1, p2) = createTestData
		import Update._
		val pe = ProductEntity
		(update(pe) set pe.name === "fast cpu" where pe.name === "cpu").run(queryDao).rowsAffected should be === 1

		mapperDao.select(ProductEntity, p1.id).get.name should be === "fast cpu"
		mapperDao.select(ProductEntity, p2.id).get.name should be === "ram"
	}

	def createTestData = {
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "red"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "10"))

		val p1 = mapperDao.insert(ProductEntity, Product("cpu", Set(a1)))
		val p2 = mapperDao.insert(ProductEntity, Product("ram", Set(a2)))
		(p1, p2)
	}
}
