package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._

/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
class ManyToOneSelfJoinQuerySpec extends SpecificationWithJUnit {

	import ManyToOneSelfJoinQuerySpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(PersonEntity, HouseEntity, AddressEntity))

	import TestQueries._
	import mapperDao._
	import queryDao._

	"self join query on house" in {
		createTables
		val a0 = insert(AddressEntity, Address(100, "SE1 1AA"))
		val a1 = insert(AddressEntity, Address(101, "SE2 2BB"))
		val h0 = insert(HouseEntity, House(10, "Appartment A", a0))
		val h1 = insert(HouseEntity, House(11, "Block B", a1))
		insert(HouseEntity, House(12, "Block B", a1))
		val p0 = insert(PersonEntity, Person(0, "p0", h0))
		val p1 = insert(PersonEntity, Person(1, "p1", h0))
		val p2 = insert(PersonEntity, Person(2, "p2", h0))
		val p3 = insert(PersonEntity, Person(3, "p3", h1))
		val p4 = insert(PersonEntity, Person(4, "p4", h1))
		query(q0).toSet must_== Set(p0, p1, p2)
	}

	def createTables {
		jdbc.update("drop table if exists Address cascade")
		jdbc.update("""
			create table Address (
				id int not null,
				postcode varchar(8) not null,
				primary key (id)
			)
		""")
		jdbc.update("drop table if exists House cascade")
		jdbc.update("""
			create table House (
				id int not null,
				name varchar(30) not null,
				address_id int not null,
				primary key (id),
				foreign key (address_id) references Address(id) on delete cascade
			)
		""")
		jdbc.update("drop table if exists Person cascade")
		jdbc.update("""
			create table Person (
				id int not null,
				name varchar(30) not null,
				lives_id int not null,
				primary key (id),
				foreign key (lives_id) references House(id) on delete cascade
			)
		""")

	}
}

object ManyToOneSelfJoinQuerySpec {

	object TestQueries {
		import Query._

		val pe = PersonEntity

		val q0 = {
			val ho1 = HouseEntity
			val ho2 = new HouseEntityBase
			select from pe join
				(pe, pe.lives, ho1) join
				ho2 on ho1.name <> ho2.name and ho2.id === 11
		}
	}
	case class Person(val id: Int, var name: String, lives: House)
	case class House(val id: Int, val name: String, val address: Address)
	case class Address(val id: Int, val postCode: String)

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val lives = manyToOne("lives_id", classOf[House], _.lives)
		val constructor = (m: ValuesMap) => new Person(m(id), m(name), m(lives)) with Persisted {
			val valuesMap = m
		}
	}

	class HouseEntityBase extends SimpleEntity(classOf[House]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val address = manyToOne(classOf[Address], _.address)

		val constructor = (m: ValuesMap) => new House(m(id), m(name), m(address)) with Persisted {
			val valuesMap = m
		}
	}

	val HouseEntity = new HouseEntityBase

	object AddressEntity extends SimpleEntity(classOf[Address]) {
		val id = pk("id", _.id)
		val postCode = string("postcode", _.postCode)
		val constructor = (m: ValuesMap) =>
			new Address(m(id), m(postCode)) with Persisted {
				val valuesMap = m
			}
	}
}
