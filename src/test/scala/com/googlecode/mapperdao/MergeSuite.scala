package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         21 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class MergeSuite extends FunSuite with ShouldMatchers
{
	if (Setup.database == "h2") {
		import CommonEntities._
		val (jdbc, mapperDao, _) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity))
		val company = Company("acme")

		test("merge entity") {
			createPersonCompany(jdbc)
			val person = Person("person 1", company)
			val inserted = mapperDao.insert(PersonEntity, person)
			val upd = Person("person 1 updated", inserted.company)
			val merged = mapperDao.merge(PersonEntity, upd, inserted.id)
			merged should be === upd
			mapperDao.select(PersonEntity, inserted.id).get should be === merged
		}

		test("merge and replace") {
			createPersonCompany(jdbc)
			val person = Person("person 1", company)
			val inserted = mapperDao.insert(PersonEntity, person)
			val upd = Person("person 1 updated", replace(inserted.company, inserted.company.copy(name = "updated")))
			val merged = mapperDao.merge(PersonEntity, upd, inserted.id)
			merged should be === upd
			mapperDao.select(PersonEntity, inserted.id).get should be === merged

			jdbc.queryForInt("select count(*) from company") should be(1)
		}
	}
}
