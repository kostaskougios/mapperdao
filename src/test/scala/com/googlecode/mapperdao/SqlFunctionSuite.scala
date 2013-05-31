package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.sqlfunction.{StdSqlFunctions, SqlFunction}

/**
 * @author kostantinos.kougios
 *
 *         5 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class SqlFunctionSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	if (Setup.database != "derby") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(
			CompanyEntity,
			PersonEntity,
			HusbandEntity,
			WifeEntity
		))
		val ce = CompanyEntity
		val pe = PersonEntity
		val he = HusbandEntity

		val companyAFunction = SqlFunction.with1Arg[String, Boolean]("companyA")
		val addFunction = SqlFunction.with2Args[Int, Int, Int]("addition")
		val subFunction = SqlFunction.with2Args[Int, Int, Int]("sub")

		Setup.queries(this, jdbc).update("functions")

		test("function both left and right of the query expression, right been a param") {
			createPersonCompany(jdbc)
			mapperDao.insert(CompanyEntity, Company("Company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("Company B"))

			import Query._
			import StdSqlFunctions._
			val r = (
				select
					from ce
					where (lower(ce.name) === lower("Company B"))
				).toSet(queryDao)
			r should be === Set(cb)
		}

		test("function both left and right of the query expression") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("Company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("Company B"))

			import Query._
			import StdSqlFunctions._
			val r = (
				select
					from ce
					where (lower(ce.name) === lower(ce.name))
				).toSet(queryDao)
			r should be === Set(ca, cb)
		}

		test("std upper function") {
			createPersonCompany(jdbc)
			mapperDao.insert(CompanyEntity, Company("Company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("Company B"))

			import Query._
			import StdSqlFunctions._
			val r = (
				select
					from ce
					where (upper(ce.name) === "COMPANY B")
				).toSet(queryDao)
			r should be === Set(cb)
		}

		test("std lower function with equals") {
			createPersonCompany(jdbc)
			mapperDao.insert(CompanyEntity, Company("Company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("Company B"))

			import Query._
			import StdSqlFunctions._
			val r = (
				select
					from ce
					where (lower(ce.name) === "company b")
				).toSet(queryDao)
			r should be === Set(cb)
		}

		test("std lower function with like") {
			createPersonCompany(jdbc)
			mapperDao.insert(CompanyEntity, Company("Company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("Company B"))

			import Query._
			import StdSqlFunctions._
			val r = (
				select
					from ce
					where (lower(ce.name) like "co%ny b")
				).toSet(queryDao)
			r should be === Set(cb)
		}

		test("query with nested function") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))

			import Query._
			val r = (
				select
					from ce
					where addFunction(1, subFunction(10, 9)) === cb.id
				).toSet(queryDao)
			r should be === Set(ca, cb)
		}

		test("query with nested function and column parameter") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			mapperDao.insert(CompanyEntity, Company("company B"))

			import Query._
			val r = (
				select
					from ce
					where addFunction(1, subFunction(2, ce.id)) === 2
				).toSet(queryDao)
			r should be === Set(ca)
		}

		test("query with one-to-one value") {
			createHusbandWife(jdbc)
			mapperDao.insert(HusbandEntity, Husband("husb1", 30, Wife("wife1", 29)))
			val h2 = mapperDao.insert(HusbandEntity, Husband("husb2", 25, Wife("wife2", 20)))

			import Query._
			val r = (select
				from he
				where addFunction(1, 1) === he.wife
				).toSet(queryDao)
			r should be === Set(h2)
		}

		test("query with one-to-one param") {
			createHusbandWife(jdbc)
			val h1 = mapperDao.insert(HusbandEntity, Husband("husb1", 30, Wife("wife1", 29)))
			mapperDao.insert(HusbandEntity, Husband("husb2", 25, Wife("wife2", 20)))

			import Query._
			val r = (select
				from he
				where addFunction(he.wife, 1) === 2
				).toSet(queryDao)
			r should be === Set(h1)
		}

		test("query using function with many-to-one value") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))

			val p1a = mapperDao.insert(PersonEntity, Person("person 1 - a", ca))
			val p2a = mapperDao.insert(PersonEntity, Person("person 2 - a", ca))
			mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
			mapperDao.insert(PersonEntity, Person("person 1 - b", cb))

			import Query._
			val r = (
				select
					from pe
					where addFunction(1, 1) > pe.company
				).toSet(queryDao)
			r should be === Set(p1a, p2a)
		}

		test("query using function with many-to-one param") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))

			mapperDao.insert(PersonEntity, Person("person 1 - a", ca))
			mapperDao.insert(PersonEntity, Person("person 2 - a", ca))
			val p1b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
			val p2b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))

			import Query._
			val r = (
				select
					from pe
					where addFunction(pe.company, 2) > 3
				).toSet(queryDao)
			r should be === Set(p1b, p2b)
		}

		test("query using expression, literal param literal comparison value positive") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))
			import Query._
			val r = (
				select
					from ce
					where addFunction(ca.id, 1) === cb.id
				).toSet(queryDao)
			r should be === Set(ca, cb)
		}

		test("query using expression, literal param, column comparison value positive") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))
			import Query._
			val r = (
				select
					from ce
					where addFunction(ca.id, 1) === ce.id
				).toSet(queryDao)
			r should be === Set(cb)
		}

		test("query using expression, column param positive") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))
			import Query._
			(
				select
					from ce
					where addFunction(ce.id, 1) === cb.id
				).toSet(queryDao) should be === Set(ca)
		}

		// sqlserver/oracle dont support bool type
		if (Setup.database != "sqlserver" && Setup.database != "oracle") {
			test("query using boolean function, literal param positive") {
				createPersonCompany(jdbc)
				val ca = mapperDao.insert(CompanyEntity, Company("company A"))
				val cb = mapperDao.insert(CompanyEntity, Company("company B"))

				import Query._
				(
					select
						from ce
						where companyAFunction("company A")
					).toSet(queryDao) should be === Set(ca, cb)
			}

			test("query using boolean function, literal param negative") {
				createPersonCompany(jdbc)
				mapperDao.insert(CompanyEntity, Company("company A"))
				mapperDao.insert(CompanyEntity, Company("company B"))

				import Query._
				(
					select
						from ce
						where companyAFunction("company XX")
					).toSet(queryDao) should be === Set()
			}

			test("query using boolean function, columninfo param") {
				createPersonCompany(jdbc)
				val ca = mapperDao.insert(CompanyEntity, Company("company A"))
				mapperDao.insert(CompanyEntity, Company("company B"))

				import Query._
				(
					select
						from ce
						where companyAFunction(ce.name)
					).toSet(queryDao) should be === Set(ca)
			}

			test("query using boolean function with join") {
				createPersonCompany(jdbc)
				val ca = mapperDao.insert(CompanyEntity, Company("company A"))
				val cb = mapperDao.insert(CompanyEntity, Company("company B"))

				val p1a = mapperDao.insert(PersonEntity, Person("person 1 - a", ca))
				val p2a = mapperDao.insert(PersonEntity, Person("person 2 - a", ca))
				mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
				mapperDao.insert(PersonEntity, Person("person 1 - b", cb))

				import Query._
				(
					select
						from pe
						join(pe, pe.company, ce)
						where companyAFunction(ce.name)
					).toSet(queryDao) should be === Set(p1a, p2a)
			}
		}
	}
}
