package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 14 Aug 2011
 */
class ManyToOneAndOneToManyCyclicSpec extends SpecificationWithJUnit {
	import ManyToOneAndOneToManyCyclicSpec._
	val (jdbc, mapperDao) = setup

	"insert" in {
		createTables
		import mapperDao._

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val person = Person(10, "Coder1", company)
		insert(PersonEntity, person) must_== person
	}

	"select" in {
		createTables
		import mapperDao._

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val inserted = insert(PersonEntity, Person(10, "Coder1", company))

		select(PersonEntity, 10).get must_== Person(10, "Coder1", Company(1, "Coders Ltd", List(Person(10, "Coder1", null))))
	}

	"update" in {
		createTables
		import mapperDao._

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		insert(PersonEntity, Person(10, "Coder1", company))

		val selected = select(PersonEntity, 10).get

		val updated = update(PersonEntity, selected, Person(10, "Coder1-changed", company))
		updated must_== Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))

		select(CompanyEntity, 1).get must_== Company(1, "Coders Ltd", List(Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))))
	}

	def createTables =
		{
			jdbc.update("drop table if exists Person cascade")
			jdbc.update("drop table if exists Company cascade")

			jdbc.update("""
					create table Company (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Person (
						id int not null,
						name varchar(100) not null,
						company_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade
					)
			""")
		}

	def setup =
		{
			val typeRegistry = TypeRegistry(PersonEntity, CompanyEntity)

			Setup.setupMapperDao(typeRegistry)
		}
}

object ManyToOneAndOneToManyCyclicSpec {
	case class Person(val id: Int, val name: String, val company: Company)
	case class Company(val id: Int, val name: String, employees: List[Person])

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val company = manyToOne("company_id", classOf[Company], _.company)

		val constructor = (m: ValuesMap) => new Person(m(id), m(name), m(company)) with Persisted {
			val valuesMap = m
		}
	}

	object CompanyEntity extends SimpleEntity(classOf[Company]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val employees = oneToMany(classOf[Person], "company_id", _.employees)
		val constructor = (m: ValuesMap) => new Company(m(id), m(name), m(employees).toList) with Persisted {
			val valuesMap = m
		}
	}

}