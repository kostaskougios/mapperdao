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

		jdbc.update("""
CREATE or replace FUNCTION companyA(IN cname varchar(50)) RETURNS boolean AS
$$
begin
	return cname = 'company A';
end
$$

LANGUAGE plpgsql VOLATILE;
""")
		test("query using function") {
			createPersonCompany(jdbc)
			val ca = mapperDao.insert(CompanyEntity, Company("company A"))
			val cb = mapperDao.insert(CompanyEntity, Company("company B"))

			val p1a = mapperDao.insert(PersonEntity, Person("person 1 - a", ca))
			val p2a = mapperDao.insert(PersonEntity, Person("person 2 - a", ca))
			val p1b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
			val p2b = mapperDao.insert(PersonEntity, Person("person 1 - b", cb))
		}
	}
}
