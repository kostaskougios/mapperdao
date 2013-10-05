package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         14 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneAndOneToManyCyclicSuite extends FunSuite with ShouldMatchers
{

	import ManyToOneAndOneToManyCyclicSuite._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(PersonEntity, CompanyEntity))

	if (Setup.database != "derby") {
		test("update id of one-to-many") {
			createTables
			val company = mapperDao.insert(CompanyEntity, Company(1, "Coders Ltd", List()))
			val inserted = mapperDao.insert(PersonEntity, Person(10, "Coder1", company))

			// reload company to get the actual state of the entity
			val companyReloaded = mapperDao.select(CompanyEntity, 1).get
			val updated = mapperDao.update(CompanyEntity, companyReloaded, Company(5, "Coders Ltd", companyReloaded.employees))
			updated should be === Company(5, "Coders Ltd", List(inserted))
			mapperDao.select(CompanyEntity, 5).get should be === Company(5, "Coders Ltd", List(Person(10, "Coder1", Company(5, "Coders Ltd", List())))) // Company(5, "Coders Ltd", List() is a mock object due to the cyclic dependencies
			mapperDao.select(CompanyEntity, 1) should be(None)
		}
	}

	test("update id of many-to-one") {
		createTables
		val company = mapperDao.insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val inserted = mapperDao.insert(PersonEntity, Person(10, "Coder1", company))
		val updated = mapperDao.update(PersonEntity, inserted, Person(15, "Coder1", inserted.company))
		updated should be === Person(15, "Coder1", inserted.company)
		mapperDao.select(PersonEntity, 15).get should be === Person(15, "Coder1",
			Company(1, "Coders Ltd",
				List(Person(15, "Coder1", Company(1, "Coders Ltd", List()))
				)
			)
		)
		mapperDao.select(PersonEntity, 10) should be(None)
	}

	test("insert") {
		createTables

		val company = mapperDao.insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val person = Person(10, "Coder1", company)
		mapperDao.insert(PersonEntity, person) should be === person
	}

	test("select") {
		createTables

		val company = mapperDao.insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		mapperDao.insert(PersonEntity, Person(10, "Coder1", company))

		mapperDao.select(PersonEntity, 10).get should be === Person(10, "Coder1",
			Company(1, "Coders Ltd",
				List(
					Person(10, "Coder1", Company(1, "Coders Ltd", List()))
				)
			)
		)
	}

	test("update") {
		createTables

		val company = mapperDao.insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		mapperDao.insert(PersonEntity, Person(10, "Coder1", company))

		val selected = mapperDao.select(PersonEntity, 10).get

		val updated = mapperDao.update(PersonEntity, selected, Person(10, "Coder1-changed", company))
		updated should be === Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))

		mapperDao.select(CompanyEntity, 1).get should be === Company(1, "Coders Ltd", List(Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))))
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object ManyToOneAndOneToManyCyclicSuite
{

	case class Person(id: Int, name: String, company: Company)

	case class Company(id: Int, name: String, employees: List[Person])

	object PersonEntity extends Entity[Int, SurrogateIntId, Person]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val company = manytoone(CompanyEntity) to (_.company)

		def constructor(implicit m) = new Person(id, name, company) with SurrogateIntId
	}

	object CompanyEntity extends Entity[Int, SurrogateIntId, Company]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val employees = onetomany(PersonEntity) to (_.employees)

		def constructor(implicit m) = new Company(id, name, employees) with SurrogateIntId
	}

}