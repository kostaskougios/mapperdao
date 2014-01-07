package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

/**
 * @author kostantinos.kougios
 *
 *         28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneSelfJoinQuerySuite extends FunSuite with Matchers
{

	import ManyToOneSelfJoinQuerySuite._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(PersonEntity, HouseEntity, AddressEntity))

	import Query._

	val pe = PersonEntity
	val ho1 = HouseEntity

	test("self join query on house") {
		createTables()
		val List(a0, a1) = mapperDao.insertBatch(AddressEntity, List(
			Address(100, "SE1 1AA"),
			Address(101, "SE2 2BB")
		))
		val List(h0, h1, _) = mapperDao.insertBatch(HouseEntity,
			List(
				House(10, "Appartment A", a0),
				House(11, "Block B", a1),
				House(12, "Block B", a1)
			))
		val List(p0, p1, p2, _, _) = mapperDao.insertBatch(PersonEntity,
			List(
				Person(0, "p0", h0),
				Person(1, "p1", h0),
				Person(2, "p2", h0),
				Person(3, "p3", h1),
				Person(4, "p4", h1)
			))
		queryDao.query(
			select from pe
				join(pe, pe.lives, ho1)
				join (HouseEntity as 'he) on ho1.name <>('he, ho1.name) and ('he, ho1.id) === 11
		).toSet should be(Set(p0, p1, p2))
	}

	def createTables() {
		Setup.dropAllTables(jdbc)
		jdbc.update( """
			create table Address (
				id int not null,
				postcode varchar(8) not null,
				primary key (id)
			)""")
		jdbc.update( """
			create table House (
				id int not null,
				name varchar(30) not null,
				address_id int not null,
				primary key (id),
				foreign key (address_id) references Address(id) on delete cascade
			)""")
		jdbc.update( """
			create table Person (
				id int not null,
				name varchar(30) not null,
				lives_id int not null,
				primary key (id),
				foreign key (lives_id) references House(id) on delete cascade
			)""")
	}
}

object ManyToOneSelfJoinQuerySuite
{

	case class Person(id: Int, var name: String, lives: House)

	case class House(id: Int, name: String, address: Address)

	case class Address(id: Int, postCode: String)

	object PersonEntity extends Entity[Int, SurrogateIntId, Person]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val lives = manytoone(HouseEntity) foreignkey "lives_id" to (_.lives)

		def constructor(implicit m: ValuesMap) = new Person(id, name, lives) with Stored
	}

	class HouseEntityBase extends Entity[Int, SurrogateIntId, House]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val address = manytoone(AddressEntity) to (_.address)

		def constructor(implicit m: ValuesMap) = new House(id, name, address) with Stored
	}

	val HouseEntity = new HouseEntityBase

	object AddressEntity extends Entity[Int, SurrogateIntId, Address]
	{
		val id = key("id") to (_.id)
		val postCode = column("postcode") to (_.postCode)

		def constructor(implicit m: ValuesMap) = new Address(id, postCode) with Stored
	}

}
