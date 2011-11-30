package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.scala_tools.time.Imports._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneSelfJoinQuerySpec extends SpecificationWithJUnit {

	import ManyToOneSelfJoinQuerySpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, HouseEntity, AddressEntity))

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
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val lives = manytoone(HouseEntity) foreignkey "lives_id" to (_.lives)
		def constructor(implicit m) = new Person(id, name, lives) with Persisted
	}

	class HouseEntityBase extends SimpleEntity(classOf[House]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val address = manytoone(AddressEntity) to (_.address)

		def constructor(implicit m) = new House(id, name, address) with Persisted
	}

	val HouseEntity = new HouseEntityBase

	object AddressEntity extends SimpleEntity(classOf[Address]) {
		val id = key("id") to (_.id)
		val postCode = column("postcode") to (_.postCode)
		def constructor(implicit m) = new Address(id, postCode) with Persisted
	}
}
