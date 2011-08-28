package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.matcher.MatchResult
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup
/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
class OneToManyQuerySpec extends SpecificationWithJUnit {
	import OneToManyQuerySpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(HouseEntity, PersonEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

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
		jdbc.update("drop table if exists Person cascade")
		jdbc.update("""
			create table Person (
				id int not null,
				name varchar(100) not null,
				primary key (id)
			)
		""")

		jdbc.update("drop table if exists House cascade")
		jdbc.update("""
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade on update cascade
			)
		""")
	}
}

object OneToManyQuerySpec {

	object TestQueries {
		import Query._

		val p = PersonEntity
		val h = HouseEntity

		def q0 = select from p join p.owns where h.address === "London"
		def q1 = select from p join p.owns where (h.address === "Madrid" or h.address === "Rome") and h.id >= 6
	}

	case class Person(val id: Int, var name: String, owns: Set[House])
	case class House(val id: Int, val address: String)

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = pk("id", _.id)
		val address = string("address", _.address)

		val constructor = (m: ValuesMap) ⇒ new House(m(id), m(address)) with Persisted {
			val valuesMap = m
		}
	}

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val owns = oneToMany("housesAlias", classOf[House], "person_id", _.owns)

		val constructor = (m: ValuesMap) => new Person(m(id), m(name), m(owns).toSet) with Persisted {
			val valuesMap = m
		}
	}
}