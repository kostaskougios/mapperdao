package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 13 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneSpec extends SpecificationWithJUnit {
	import ManyToOneSpec._
	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity, HouseEntity))

	if (Setup.database != "derby") {
		"update id's" in {
			createTables

			val company = Company(5, "Coders limited")
			val house = House(8, "Rhodes,Greece")
			val person = Person(2, "Kostas", company, house)

			val inserted = mapperDao.insert(PersonEntity, person)
			mapperDao.update(HouseEntity, inserted.lives, House(7, "Rhodes,Greece"))
			mapperDao.select(PersonEntity, 2).get must_== Person(2, "Kostas", company, House(7, "Rhodes,Greece"))
		}
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
			Setup.queries(this, jdbc).update("ddl")
		}
}

object ManyToOneSpec {
	case class Person(val id: Int, val name: String, val company: Company, val lives: House)
	case class Company(val id: Int, val name: String)
	case class House(val id: Int, val address: String)

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val company = manytoone(CompanyEntity) to (_.company)
		val lives = manytoone(HouseEntity) to (_.lives)

		def constructor(implicit m) = new Person(id, name, company, lives) with Persisted
	}

	object CompanyEntity extends SimpleEntity(classOf[Company]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)

		def constructor(implicit m) = new Company(id, name) with Persisted
	}

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = key("id") to (_.id)
		val address = column("address") to (_.address)
		def constructor(implicit m) = new House(id, address) with Persisted
	}
}