package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 *         13 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneSuite extends FunSuite with ShouldMatchers {

	import ManyToOneSpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, CompanyEntity, HouseEntity))

	if (Setup.database != "derby") {
		test("update id's") {
			createTables

			val company = Company(5, "Coders limited")
			val house = House(8, "Rhodes,Greece")
			val person = Person(2, "Kostas", company, house)

			val inserted = mapperDao.insert(PersonEntity, person)
			mapperDao.update(HouseEntity, Helpers.asSurrogateIntId(inserted.lives), House(7, "Rhodes,Greece"))
			mapperDao.select(PersonEntity, 2).get should be === Person(2, "Kostas", company, House(7, "Rhodes,Greece"))
		}
	}

	test("batch insert, new one-part") {
		createTables
		val c1 = Company(101, "C1")
		val c2 = Company(102, "C2")

		val h1 = House(201, "H1")
		val h2 = House(202, "H2")

		val p1 = Person(1, "P1", c1, h1)
		val p2 = Person(2, "P2", c2, h2)
		val p3 = Person(3, "P2", c2, h1)

		val List(i1, i2, i3) = mapperDao.insertBatch(PersonEntity, List(p1, p2, p3))
		i1 should be(p1)
		i2 should be(p2)
		i3 should be(p3)

		mapperDao.select(PersonEntity, i1.id).get should be(i1)
		mapperDao.select(PersonEntity, i2.id).get should be(i2)
		mapperDao.select(PersonEntity, i3.id).get should be(i3)
	}

	test("insert") {
		createTables

		val company = Company(5, "Coders limited")
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = mapperDao.insert(PersonEntity, person)
		inserted should be === person

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) should be(None)
	}

	test("insert with existing foreign entity") {
		createTables

		import mapperDao._
		val company = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = insert(PersonEntity, person)
		inserted should be === person

		val selected = select(PersonEntity, 2).get
		selected should be === inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) should be(None)
	}

	test("select") {
		createTables

		import mapperDao._
		val company = Company(5, "Coders limited")
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company, house)

		val inserted = insert(PersonEntity, person)

		val selected = select(PersonEntity, 2).get
		selected should be === inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) should be(None)
	}

	test("select with null FK") {
		createTables

		import mapperDao._
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", null, house)

		val inserted = insert(PersonEntity, person)

		val selected = select(PersonEntity, 2).get
		selected should be === inserted

		mapperDao.delete(PersonEntity, inserted)
		mapperDao.select(PersonEntity, inserted.id) should be(None)
	}

	test("update") {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val company2 = insert(CompanyEntity, Company(6, "Scala Inc"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted should be === person

		val modified = Person(2, "changed", company2, inserted.lives)
		val updated = update(PersonEntity, inserted, modified)
		updated should be === modified

		val selected = select(PersonEntity, 2).get
		selected should be === updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) should be(None)
	}

	test("update to null") {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted should be === person

		val modified = Person(2, "changed", null, inserted.lives)
		val updated = update(PersonEntity, inserted, modified)
		updated should be === modified

		val selected = select(PersonEntity, 2).get
		selected should be === updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) should be(None)
	}

	test("update to null both FK") {
		createTables

		import mapperDao._
		val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
		val house = House(8, "Rhodes,Greece")
		val person = Person(2, "Kostas", company1, house)

		val inserted = insert(PersonEntity, person)
		inserted should be === person

		val modified = Person(2, "changed", null, null)
		val updated = update(PersonEntity, inserted, modified)
		updated should be === modified

		val selected = select(PersonEntity, 2).get
		selected should be === updated

		mapperDao.delete(PersonEntity, selected)
		mapperDao.select(PersonEntity, selected.id) should be(None)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object ManyToOneSpec {

	case class Person(val id: Int, val name: String, val company: Company, val lives: House)

	case class Company(val id: Int, val name: String)

	case class House(val id: Int, val address: String)

	object PersonEntity extends Entity[Int, Person] {
		type Stored = SurrogateIntId
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val company = manytoone(CompanyEntity) to (_.company)
		val lives = manytoone(HouseEntity) to (_.lives)

		def constructor(implicit m) = new Person(id, name, company, lives) with Stored
	}

	object CompanyEntity extends Entity[Int, Company] {
		type Stored = SurrogateIntId
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)

		def constructor(implicit m) = new Company(id, name) with Stored
	}

	object HouseEntity extends Entity[Int, House] {
		type Stored = SurrogateIntId
		val id = key("id") to (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m) = new House(id, address) with Stored
	}

}