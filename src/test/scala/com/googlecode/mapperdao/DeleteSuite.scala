package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.exceptions.ColumnNotPartOfQueryException

/**
 * @author kostantinos.kougios
 *
 *         17 Oct 2012
 */
@RunWith(classOf[JUnitRunner])
class DeleteSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(AllEntities)

	test("delete with errors") {
		createProductAttribute(jdbc)
		createTestData

		import Delete._
		val pe = ProductEntity
		val ae = AttributeEntity
		intercept[ColumnNotPartOfQueryException] {
			(delete from pe where ae.name === "colour").run(queryDao).rowsAffected should be(1)
		}
	}

	test("delete with where referencing related one-to-one") {
		createHusbandWife(jdbc)

		val h1 = mapperDao.insert(HusbandEntity, Husband("husband1", 30, Wife("wife1", 30)))
		val h2 = mapperDao.insert(HusbandEntity, Husband("husband2", 29, Wife("wife2", 29)))

		val he = HusbandEntity

		{
			import Delete._
			(delete from he where he.wife === h1.wife).run(queryDao).rowsAffected should be(1)
		}

		{
			import Query._
			(select from he where he.name === "husband1").toList(queryDao) should be(Nil)
			(select from he where he.name === "husband2").toList(queryDao) should be(List(h2))
		}
	}

	test("delete with where referencing related many-to-one") {
		createPersonCompany(jdbc)

		val c1 = mapperDao.insert(CompanyEntity, Company("acme"))
		val c2 = mapperDao.insert(CompanyEntity, Company("8bit soft"))
		val p1 = mapperDao.insert(PersonEntity, Person("person1", c1))
		val p2 = mapperDao.insert(PersonEntity, Person("person2", c1))
		val p3 = mapperDao.insert(PersonEntity, Person("person2", c2))

		import Delete._
		val pe = PersonEntity
		(delete from pe where pe.company === c1).run(queryDao).rowsAffected should be(2)
		mapperDao.select(pe, p1.id) should be(None)
		mapperDao.select(pe, p2.id) should be(None)
		mapperDao.select(pe, p3.id).get should be(p3)
	}

	test("delete all") {
		createProductAttribute(jdbc)
		createTestData

		import Delete._
		val pe = ProductEntity
		(delete from pe).run(queryDao).rowsAffected should be(2)

		import Query._
		(select from pe).toList(queryDao) should be(Nil)
	}

	test("delete simple where clause") {
		createProductAttribute(jdbc)
		val (_, p2) = createTestData

		import Delete._
		val pe = ProductEntity
		(delete from pe where pe.name === "cpu").run(queryDao).rowsAffected should be(1)

		import Query._
		(select from pe).toList(queryDao) should be(List(p2))
	}

	def createTestData = {
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "red"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "10"))

		val p1 = mapperDao.insert(ProductEntity, Product("cpu", Set(a1)))
		val p2 = mapperDao.insert(ProductEntity, Product("ram", Set(a2)))
		(p1, p2)
	}
}
