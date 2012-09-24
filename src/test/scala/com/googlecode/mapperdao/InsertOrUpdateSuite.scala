package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 21 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class InsertOrUpdateSuite extends FunSuite with ShouldMatchers {
	if (Setup.database == "h2") {
		import CommonEntities._
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity))
		val company = Company("acme")

		test("insertOrUpdate for new object, no id's") {
			createPersonCompany(jdbc)
			val person = Person("person 1", company)
			val inserted = mapperDao.insertOrUpdate(PersonEntity, person, None)
			inserted should be === person
			mapperDao.select(PersonEntity, inserted.id).get should be === inserted
		}

		test("insertOrUpdate for new object, with id's") {
			createPersonCompany(jdbc)
			val person = Person("person 1", company)
			val inserted = mapperDao.insertOrUpdate(PersonEntity, person, Some(5))
			inserted should be === person
			mapperDao.select(PersonEntity, inserted.id).get should be === inserted
		}
	}
}
