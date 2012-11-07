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

	test("update 2 columns") {
		createHusbandWife(jdbc)
		val h1 = mapperDao.insert(HusbandEntity, Husband("h1", 30, Wife("w1", 29)))
		val h2 = mapperDao.insert(HusbandEntity, Husband("h2", 40, Wife("w2", 39)))
		import Update._
		val he = HusbandEntity
		(
			update(he)
			set (he.name === "x", he.age === 29)
			where he.age === 30
		).run(queryDao).rowsAffected should be(1)

		import Query._
		(
			select
			from he
			where he.name === "x"
		).toSet(queryDao) should be(Set(Husband("x", 29, Wife("w1", 29))))
	}

	test("update one-to-one") {
		createHusbandWife(jdbc)
		val w3 = mapperDao.insert(WifeEntity, Wife("w3", 25))
		val h1 = mapperDao.insert(HusbandEntity, Husband("h1", 30, Wife("w1", 29)))
		val h2 = mapperDao.insert(HusbandEntity, Husband("h2", 40, Wife("w2", 39)))

		import Update._
		val he = HusbandEntity
		(
			update(he)
			set he.wife === w3
			where he.age === 30
		).run(queryDao).rowsAffected should be(1)

		import Query._
		(
			select
			from he
			where he.name === "h1"
		).toSet(queryDao) should be(Set(Husband("h1", 30, w3)))
	}

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
