package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToManyQuerySuite extends FunSuite
{

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(HouseEntity, PersonEntity))
	val p = PersonEntity
	val h = HouseEntity

	test("self join, aliases") {
		createTables()
		val List(p0, p1, _) = mapperDao.insertBatch(PersonEntity,
			List(
				Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))),
				Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))),
				Person(7, "person2", Set(House(5, "Rome"), House(6, "Berlin")))
			)
		)
		import Query._
		queryDao.query(
			select from p
				join(p, p.owns, h)
				join (p as 'p1) on p.id <>('p1, p.id)
				join(p as 'p1, p.owns, h as 'h1) on h.address ===('h1, h.address)
		).toSet should be(Set(p0, p1))
	}

	test("join unlrelated table") {
		createTables()
		val List(p0, p1, _) = mapperDao.insertBatch(PersonEntity,
			List(
				Person(5, "Berlin", Set(House(1, "London"), House(2, "Paris"))),
				Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))),
				Person(7, "person2", Set(House(5, "Rome"), House(6, "Berlin")))
			)
		)
		import Query._
		queryDao.query(
			select from p
				join h on h.address === p.name
		) should be(List(p0))
	}

	test("with aliases") {
		createTables()
		val List(p0, _, _) = mapperDao.insertBatch(PersonEntity,
			List(
				Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))),
				Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))),
				Person(7, "person2", Set(House(5, "Rome"), House(6, "Athens")))
			)
		)
		import Query._
		queryDao.query(
			select from p
				join(p, p.owns, h)
				join(p, p.owns, h as 'x)
				where h.address === "London" and ('x, h.address) === "Paris"
		) should be(List(p0))
	}

	test("query with limits (offset only)") {
		createTables()
		val persons = for (i <- 0 to 10) yield mapperDao.insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		import Query._
		queryDao.query(QueryConfig(offset = Some(7)), select from p).toSet should be(Set(persons(7), persons(8), persons(9), persons(10)))
	}

	test("query with limits (limit only)") {
		createTables()
		val persons = for (i <- 0 to 10) yield mapperDao.insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		import Query._
		queryDao.query(QueryConfig(limit = Some(2)), select from p).toSet should be(Set(persons(0), persons(1)))
	}

	test("query with limits") {
		createTables()
		val persons = for (i <- 0 to 10) yield mapperDao.insert(PersonEntity, Person(i, "person%d".format(i), Set(House(i * 2, "London"), House(i * 2 + 1, "Paris"))))
		import Query._
		queryDao.query(QueryConfig(offset = Some(5), limit = Some(2)), select from p).toSet should be === Set(persons(5), persons(6))
	}

	test("query with skip") {
		createTables()
		mapperDao.insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		mapperDao.insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))
		mapperDao.insert(PersonEntity, Person(7, "person2", Set(House(5, "Rome"), House(6, "Athens"))))

		import Query._
		queryDao.query(
			QueryConfig(skip = Set(PersonEntity.owns)),
			select from p join(p, p.owns, h) where h.address === "London"
		).toSet should be === Set(Person(5, "person0", Set()), Person(6, "person1", Set()))
	}

	test("based on FK") {
		createTables()
		val p0 = mapperDao.insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = mapperDao.insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))

		import Query._
		val q1 = select from p where p.owns === p0.owns.head
		queryDao.query(q1) should be === List(p0)
		queryDao.query(select from p where p.owns === p1.owns.head) should be(List(p1))
	}

	test("based on FK, not equals") {
		createTables()
		val p0 = mapperDao.insert(PersonEntity, Person(5, "person0", Set(House(1, "London"))))
		val p1 = mapperDao.insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))

		import Query._
		queryDao.query(select from p where p.owns <> p0.owns.head).toSet should be(Set(p1))
	}

	test("join 1 level") {
		createTables()
		val p0 = mapperDao.insert(PersonEntity, Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))))
		val p1 = mapperDao.insert(PersonEntity, Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))))
		mapperDao.insert(PersonEntity, Person(7, "person2", Set(House(5, "Rome"), House(6, "Athens"))))

		import Query._
		queryDao.query(select from p join(p, p.owns, h) where h.address === "London").toSet should be(Set(p0, p1))
	}

	test("join with 2 conditions") {
		createTables()
		mapperDao.insertBatch(PersonEntity,
			List(
				Person(5, "person0", Set(House(1, "London"), House(2, "Paris"))),
				Person(6, "person1", Set(House(3, "London"), House(4, "Athens"))),
				Person(7, "person2", Set(House(5, "Rome"), House(6, "Sofia")))
			)
		)
		val p3 = mapperDao.insert(PersonEntity, Person(8, "person3", Set(House(7, "Madrid"))))

		import Query._
		queryDao.query(
			select from p
				join(p, p.owns, h)
				where (h.address === "Madrid" or h.address === "Rome")
				and h.id >= 6
		) should be(List(p3))
	}

	def createTables() {
		Setup.dropAllTables(jdbc)
		jdbc.update( """
			create table Person (
				id int not null,
				name varchar(100) not null,
				primary key (id)
			)
					 """)

		jdbc.update( """
			create table House (
				id int not null,
				address varchar(100) not null,
				person_id int not null,
				primary key (id),
				constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade
			)
					 """)
	}

	case class Person(id: Int, var name: String, owns: Set[House])

	case class House(id: Int, address: String)

	object HouseEntity extends Entity[Int, SurrogateIntId, House]
	{
		val id = key("id") to (_.id)
		val address = column("address") to (_.address)

		def constructor(implicit m: ValuesMap) = new House(id, address) with Stored
	}

	object PersonEntity extends Entity[Int, SurrogateIntId, Person]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val owns = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m: ValuesMap) = new Person(id, name, owns) with Stored
	}

}