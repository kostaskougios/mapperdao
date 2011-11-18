package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 20 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneQuerySpec extends SpecificationWithJUnit {
	import ManyToOneQuerySpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(PersonEntity, HouseEntity, AddressEntity))

	import MTOQuerySpec._
	import mapperDao._
	import queryDao._

	"query with limits (offset only)" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1

		query(QueryConfig(offset = Some(2)), q0Limits).toSet must_== Set(p2, p3, p4)
	}

	"query with limits (limit only)" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1

		query(QueryConfig(limit = Some(2)), q0Limits).toSet must_== Set(p0, p1)
	}

	"query with limits" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1

		query(QueryConfig(offset = Some(2), limit = Some(1)), q0Limits).toSet must_== Set(p2)
	}

	"query with skip" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1

		query(QueryConfig(skip = Set(PersonEntity.lives)), q0ForSkip) must_== List(Person(3, "p3", null), Person(4, "p4", null))
	}

	"query on FK for null" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1
		val p5 = insert(PersonEntity, Person(5, "p5", null))
		val p6 = insert(PersonEntity, Person(6, "p6", null))
		query(q3(null)) must_== List(p5, p6)
		query(q3n(null)) must_== List(p0, p1, p2, p3, p4)
	}

	"query on FK" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1
		query(q3(p4.lives)) must_== List(p3, p4)
		query(q3(p0.lives)) must_== List(p0, p1, p2)
		query(q3n(p0.lives)) must_== List(p3, p4)
	}

	"query 1 level join" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1

		query(q0) must_== List(p3, p4)
	}

	"query 2 level join" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1
		query(q1) must_== List(p0, p1, p2)
	}

	"query 2 level join with or" in {
		createTables
		val (p0, p1, p2, p3, p4) = testData1
		query(q2) must_== List(p0, p1, p2, p3, p4)
	}

	def testData1 = {
		createTables
		val a0 = insert(AddressEntity, Address(100, "SE1 1AA"))
		val a1 = insert(AddressEntity, Address(101, "SE2 2BB"))
		val h0 = insert(HouseEntity, House(10, "Appartment A", a0))
		val h1 = insert(HouseEntity, House(11, "Block B", a1))
		val p0 = insert(PersonEntity, Person(0, "p0", h0))
		val p1 = insert(PersonEntity, Person(1, "p1", h0))
		val p2 = insert(PersonEntity, Person(2, "p2", h0))
		val p3 = insert(PersonEntity, Person(3, "p3", h1))
		val p4 = insert(PersonEntity, Person(4, "p4", h1))
		(p0, p1, p2, p3, p4)
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		jdbc.update("""
			create table Address (
				id int not null,
				postcode varchar(8) not null,
				primary key (id)
			)
		""")
		jdbc.update("""
			create table House (
				id int not null,
				name varchar(30) not null,
				address_id int not null,
				primary key (id),
				foreign key (address_id) references Address(id) on delete cascade
			)
		""")
		jdbc.update("""
			create table Person (
				id int not null,
				name varchar(30) not null,
				lives_id int,
				primary key (id),
				foreign key (lives_id) references House(id) on delete cascade
			)
		""")
	}
}

object ManyToOneQuerySpec {

	object MTOQuerySpec {
		import Query._

		val pe = PersonEntity
		val he = HouseEntity
		val ad = AddressEntity

		val q0 = select from pe join (pe, pe.lives, he) where he.name === "Block B"
		val q0Limits = select from pe
		val q0ForSkip = select from pe join (pe, pe.lives, he) where he.name === "Block B"

		val q1 = select from pe join
			(pe, pe.lives, he) join
			(he, he.address, ad) where
			ad.postCode === "SE1 1AA"

		val q2 = select from pe join
			(pe, pe.lives, he) join
			(he, he.address, ad) where
			ad.postCode === "SE1 1AA" or
			ad.postCode === "SE2 2BB"

		def q3(h: House) = (
			select from pe
			where pe.lives === h
		)
		def q3n(h: House) = (
			select from pe
			where pe.lives <> h
		)
	}

	case class Person(val id: Int, var name: String, lives: House)
	case class House(val id: Int, val name: String, val address: Address)
	case class Address(val id: Int, val postCode: String)

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val lives = manyToOne("lives_id", HouseEntity, _.lives)
		def constructor(implicit m: ValuesMap) = new Person(id, name, lives) with Persisted
	}

	class HouseEntityBase extends SimpleEntity(classOf[House]) {
		val id = intPK("id", _.id)
		val name = string("name", _.name)
		val address = manyToOne(AddressEntity, _.address)

		def constructor(implicit m: ValuesMap) = new House(id, name, address) with Persisted
	}

	val HouseEntity = new HouseEntityBase

	object AddressEntity extends SimpleEntity(classOf[Address]) {
		val id = intPK("id", _.id)
		val postCode = string("postcode", _.postCode)
		def constructor(implicit m: ValuesMap) =
			new Address(id, postCode) with Persisted
	}
}