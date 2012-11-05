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

	test("update many-to-one") {
		createPersonCompany(jdbc)
		val c1 = mapperDao.insert(CompanyEntity, Company("c1"))
		val c2 = mapperDao.insert(CompanyEntity, Company("c2"))
		val p1 = mapperDao.insert(PersonEntity, Person("p1", c1))
		val p2 = mapperDao.insert(PersonEntity, Person("p2", c1))

		import Update._
		val pe = PersonEntity
		(
			update(pe)
			set pe.company === c2
			where pe.name === "p1"
		).run(queryDao).rowsAffected should be === 1
		mapperDao.select(PersonEntity, p1.id).get.company should be(c2)
		mapperDao.select(PersonEntity, p2.id).get.company should be(c1)
	}

	test("update all") {
		createProductAttribute(jdbc)
		val (p1, p2) = createTestData
		import Update._
		val pe = ProductEntity
		(update(pe) set pe.name === "fast cpu").run(queryDao).rowsAffected should be === 2

		mapperDao.select(ProductEntity, p1.id).get.name should be === "fast cpu"
		mapperDao.select(ProductEntity, p2.id).get.name should be === "fast cpu"
	}

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
