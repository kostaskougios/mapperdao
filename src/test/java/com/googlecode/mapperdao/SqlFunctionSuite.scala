package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 5 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class SqlFunctionSuite extends FunSuite with ShouldMatchers {
	import CommonEntities._

	if (Setup.database == "postgresql") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(CompanyEntity, PersonEntity))
		val ce = CompanyEntity
		val pe = PersonEntity

		jdbc.update("""
CREATE or replace FUNCTION companyA(IN cname varchar(50)) RETURNS boolean AS
$$
begin
	return cname = 'company A';
end
$$

LANGUAGE plpgsql VOLATILE;
""")
		jdbc.update("""
CREATE or replace FUNCTION add(IN v int, IN howMany int) RETURNS int AS
$$
begin
	return v + howMany;
end
$$

LANGUAGE plpgsql VOLATILE;
""")

		val companyAFunction = SqlFunction.with1Arg[String, Boolean]("companyA")
		val addFunction = SqlFunction.with2Args[Int, Int, Int]("add")

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
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))

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
			val p1b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
			val p2b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))

			import Query._
			(
				select
				from pe
				join (pe, pe.company, ce)
				where companyAFunction(ce.name)
			).toSet(queryDao) should be === Set(p1a, p2a)
		}
	}
}
