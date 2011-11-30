package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.matcher.MatchResult
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToManyQuerySpec extends SpecificationWithJUnit {
	import OneToManyQuerySpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(HouseEntity, PersonEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

	"query with limits (offset only)" in {
		createTables
		val persons = for (i <- 0 to 10) yield insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		query(QueryConfig(offset = Some(7)), q0Limits).toSet must_== Set(persons(7), persons(8), persons(9), persons(10))
	}

	"query with limits (limit only)" in {
		createTables
		val persons = for (i <- 0 to 10) yield insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		query(QueryConfig(limit = Some(2)), q0Limits).toSet must_== Set(persons(0), persons(1))
	}

	"query with limits" in {
		createTables
		val persons = for (i <- 0 to 10) yield insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		query(QueryConfig(offset = Some(5), limit = Some(2)), q0Limits).toSet must_== Set(persons(5), persons(6))
	}

	"query with skip" in {
		createTables
		val p0 = insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))
		val p2 = insert(PersonEntity, Person(7, "person2", Set(House(5, "Rome"), House(6, "Athens"))))

		query(QueryConfig(skip = Set(PersonEntity.owns)), q0WithSkip).toSet must_== Set(Person(5, "person0", Set()), Person(6, "person1", Set()))
	}

	"based on FK" in {
		createTables
		val p0 = insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))

		query(q2(p0.owns.head)) must_== List(p0)
		query(q2(p1.owns.head)) must_== List(p1)
	}

	"based on FK, not equals" in {
		createTables
		val p0 = insert(PersonEntity, Person(5, "person0", Set(House(1, "London"))))
		val p1 = insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))

		query(q2n(p0.owns.head)).toSet must_== Set(p1)
	}

	"join 1 level" in {
		createTables
		val p0 = insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))
		val p2 = insert(PersonEntity, Person(7, "person2", Set(House(5, "Rome"), House(6, "Athens"))))

		query(q0).toSet must_== Set(p0, p1)
	}

	"join with 2 conditions" in {
		createTables
		val p0 = insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))
		val p2 = insert(PersonEntity, Person(7, "person2", Set(House(5, "Rome"), House(6, "Sofia"))))
		val p3 = insert(PersonEntity, Person(8, "person3", Set(House(7, "Madrid"))))

		query(q1).toSet must_== Set(p3)
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				primary key (id)
			)
		""")

		jdbc.update("""
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade
			)
		""")
	}
}

object OneToManyQuerySpec {

	object TestQueries {
		import Query._

		val p = PersonEntity
		val h = HouseEntity

		def q0 = select from p join (p, p.owns, h) where h.address === "London"
		def q0Limits = select from p
		def q0WithSkip = select from p join (p, p.owns, h) where h.address === "London"
		def q1 = (
			select from p
			join (p, p.owns, h)
			where (h.address === "Madrid" or h.address === "Rome")
			and h.id >= 6
		)

		def q2(house: House) = select from p where p.owns === house
		def q2n(house: House) = select from p where p.owns <> house
	}

	case class Person(val id: Int, var name: String, owns: Set[House])
	case class House(val id: Int, val address: String)

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = key("id") to (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m) = new House(id, address) with Persisted
	}

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m) = new Person(id, name, owns) with Persisted
	}
}