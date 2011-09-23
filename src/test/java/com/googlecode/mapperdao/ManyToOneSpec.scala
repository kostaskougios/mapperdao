package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 13 Aug 2011
 */
class ManyToOneSpec extends SpecificationWithJUnit {
	import ManyToOneSpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity, HouseEntity))

	"update id's" in {
		createTables

		val company = Company(5, "Coders limited")
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = mapperDao.insert(PersonEntity, person)
		mapperDao.update(HouseEntity, inserted.lives, House(7, "Rhodes,Greece"))
		mapperDao.select(PersonEntity, 2).get must_== Person(2, "Kostas", company, House(7, "Rhodes,Greece"))
	}

	"insert" in {
		createTables

		val company = Company(5, "Coders limited")
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = mapperDao.insert(PersonEntity, person)
		inserted must_== person

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) must beNone
	}

	"insert with existing foreign entity" in {
		createTables

		import mapperDao._
		val company = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		val selected = select(PersonEntity, 2).get
		selected must_== inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) must beNone
	}

	"select" in {
		createTables

		import mapperDao._
		val company = Company(5, "Coders limited")
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = insert(PersonEntity, person)

		val selected = select(PersonEntity, 2).get
		selected must_== inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) must beNone
	}

	"select with null FK" in {
		createTables

		import mapperDao._
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", null, house)

		val inserted = insert(PersonEntity, person)

		val selected = select(PersonEntity, 2).get
		selected must_== inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) must beNone
	}

	"update" in {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val company2 = insert(CompanyEntity, Company(6, "Scala Inc"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		val modified = Person(2, "changed", company2, inserted.lives)
		val updated = update(PersonEntity, inserted, modified)
		updated must_== modified

		val selected = select(PersonEntity, 2).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
	}

	"update to null" in {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		val modified = Person(2, "changed", null, inserted.lives)
		val updated = update(PersonEntity, inserted, modified)
		updated must_== modified

		val selected = select(PersonEntity, 2).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
	}

	"update to null both FK" in {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted must_== person

		val modified = Person(2, "changed", null, null)
		val updated = update(PersonEntity, inserted, modified)
		updated must_== modified

		val selected = select(PersonEntity, 2).get
		selected must_== updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) must beNone
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
					create table House (
						id int not null,
						address varchar(100) not null,
						primary key(id)
					)
			""")
					jdbc.update("""
					create table Person (
						id int not null,
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade on update cascade,
						foreign key (house_id) references House(id) on delete cascade on update cascade
					)
			""") case "mysql" =>
					jdbc.update("""
					create table Company (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					) engine InnoDB
			""")
					jdbc.update("""
					create table House (
						id int not null,
						address varchar(100) not null,
						primary key(id)
					) engine InnoDB
			""")
					jdbc.update("""
					create table Person (
						id int not null,
						name varchar(100) not null,
						company_id int,
						house_id int,
						primary key(id),
						foreign key (company_id) references Company(id) on delete cascade on update cascade,
						foreign key (house_id) references House(id) on delete cascade on update cascade
					) engine InnoDB
			""")
			}
		}
}

object ManyToOneSpec {
	case class Person(val id: Int, val name: String, val company: Company, val lives: House)
	case class Company(val id: Int, val name: String)
	case class House(val id: Int, val address: String)

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val company = manyToOne(classOf[Company], _.company)
		val lives = manyToOne(classOf[House], _.lives)

		val constructor = (m: ValuesMap) => new Person(m(id), m(name), m(company), m(lives)) with Persisted {
			val valuesMap = m
		}
	}

	object CompanyEntity extends SimpleEntity(classOf[Company]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)

		val constructor = (m: ValuesMap) => new Company(m(id), m(name)) with Persisted {
			val valuesMap = m
		}
	}

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = intPK("id", _.id)
		val address = string("address", _.address)
		val constructor = (m: ValuesMap) => new House(m(id), m(address)) with Persisted {
			val valuesMap = m
		}
	}
}