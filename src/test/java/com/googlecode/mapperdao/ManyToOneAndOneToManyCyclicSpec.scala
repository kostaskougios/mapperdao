package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 14 Aug 2011
 */
class ManyToOneAndOneToManyCyclicSpec extends SpecificationWithJUnit {
	import ManyToOneAndOneToManyCyclicSpec._
	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity))

	import mapperDao._

	if (Setup.database != "derby") {
		"update id of one-to-many" in {
			createTables
			val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
			val inserted = insert(PersonEntity, Person(10, "Coder1", company))

			// reload company to get the actual state of the entity
			val companyReloaded = select(CompanyEntity, 1).get
			val updated = update(CompanyEntity, companyReloaded, Company(5, "Coders Ltd", companyReloaded.employees))
			updated must_== Company(5, "Coders Ltd", List(inserted))
			select(CompanyEntity, 5).get must_== Company(5, "Coders Ltd", List(Person(10, "Coder1", Company(5, "Coders Ltd", List())))) // Company(5, "Coders Ltd", List() is a mock object due to the cyclic dependencies
			select(CompanyEntity, 1) must beNone
		}
	}

	"update id of many-to-one" in {
		createTables
		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val inserted = insert(PersonEntity, Person(10, "Coder1", company))
		val updated = update(PersonEntity, inserted, Person(15, "Coder1", inserted.company))
		updated must_== Person(15, "Coder1", inserted.company)
		select(PersonEntity, 15).get must_== Person(15, "Coder1", Company(1, "Coders Ltd", List(Person(15, "Coder1", null)))) // please note the null is due to Person(15,"Coder1",null) been a mock object
		select(PersonEntity, 10) must beNone
	}

	"insert" in {
		createTables

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val person = Person(10, "Coder1", company)
		insert(PersonEntity, person) must_== person
	}

	"select" in {
		createTables

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		val inserted = insert(PersonEntity, Person(10, "Coder1", company))

		select(PersonEntity, 10).get must_== Person(10, "Coder1", Company(1, "Coders Ltd", List(Person(10, "Coder1", null))))
	}

	"update" in {
		createTables

		val company = insert(CompanyEntity, Company(1, "Coders Ltd", List()))
		insert(PersonEntity, Person(10, "Coder1", company))

		val selected = select(PersonEntity, 10).get

		val updated = update(PersonEntity, selected, Person(10, "Coder1-changed", company))
		updated must_== Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))

		select(CompanyEntity, 1).get must_== Company(1, "Coders Ltd", List(Person(10, "Coder1-changed", Company(1, "Coders Ltd", List()))))
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.database match {
				case "postgresql" =>
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
						foreign key (company_id) references Company(id) on delete cascade on update cascade
					)
			""")
				case "oracle" =>
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
					jdbc.update("""
						create or replace trigger cascade_update
						after update of id on Company
						for each row
						begin
							update Person
							set company_id = :new.id
							where company_id = :old.id;
						end;
					""")
				case "mysql" =>
					jdbc.update("""
					create table Company (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					) engine InnoDB
			""")
					jdbc.update("""
					create table Person (
						id int not null,
						name varchar(100) not null,
						company_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade on update cascade
					) engine InnoDB
			""")
				case "derby" =>
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
						foreign key (company_id) references Company(id) on delete cascade on update restrict
					)
			""")
			}
		}
}

object ManyToOneAndOneToManyCyclicSpec {
	case class Person(val id: Int, val name: String, val company: Company)
	case class Company(val id: Int, val name: String, employees: List[Person])

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val company = manyToOne(CompanyEntity, _.company)

		def constructor(implicit m: ValuesMap) = new Person(id, name, company) with Persisted
	}

	object CompanyEntity extends SimpleEntity(classOf[Company]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val employees = oneToMany(PersonEntity, _.employees)
		def constructor(implicit m: ValuesMap) = new Company(id, name, employees) with Persisted
	}

}