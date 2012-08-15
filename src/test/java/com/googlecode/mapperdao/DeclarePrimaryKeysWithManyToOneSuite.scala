package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scala_tools.time.Imports._
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 15 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class DeclarePrimaryKeysWithManyToOneSuite extends FunSuite with ShouldMatchers {
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, LinkedPeopleEntity))

	val person1 = Person("person1@example.com", "Mr Person One")
	val person2 = Person("person2@example.com", "Mrs Person Two")
	val person3 = Person("person3@example.com", "Mr Person Three")

	test("crud") {
		createTables()

		val p1 = mapperDao.insert(PersonEntity, person1)
		val p2 = mapperDao.insert(PersonEntity, person2)
		val p3 = mapperDao.insert(PersonEntity, person3)

		val lp1 = LinkedPeople(p1, p2, "these like each other")
		val lp1Inserted = mapperDao.insert(LinkedPeopleEntity, lp1)
		lp1Inserted should be === lp1

		// add some noise
		val lp2Inserted = mapperDao.insert(LinkedPeopleEntity, LinkedPeople(p2, p3, "p2 likes p3"))

		val selected = mapperDao.select(LinkedPeopleEntity, p1, p2).get
		selected should be === lp1Inserted

		val upd = selected.copy(from = p3, note = "now p3 likes p2")
		val updated = mapperDao.update(LinkedPeopleEntity, selected, upd)
		updated should be === upd

		// verify the update
		mapperDao.select(LinkedPeopleEntity, p1, p2) should be(None)
		val reselected = mapperDao.select(LinkedPeopleEntity, p3, p2).get
		reselected should be === updated

		// now delete
		mapperDao.delete(LinkedPeopleEntity, reselected)
		mapperDao.select(LinkedPeopleEntity, p3, p2) should be(None)
		mapperDao.select(LinkedPeopleEntity, p2, p3).get should be === lp2Inserted
	}

	def createTables() {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Person(email: String, name: String)
	case class LinkedPeople(from: Person, to: Person, note: String)

	object PersonEntity extends SimpleEntity[Person] {
		val email = key("email") to (_.email)
		val name = column("name") to (_.name)

		def constructor(implicit m: ValuesMap) = {
			new Person(email, name) with Persisted
		}
	}

	object LinkedPeopleEntity extends SimpleEntity[LinkedPeople] {
		val from = manytoone(PersonEntity) foreignkey ("from_id") to (_.from)
		val to = manytoone(PersonEntity) foreignkey ("to_id") to (_.to)
		val note = column("note") to (_.note)

		declarePrimaryKey(from)
		declarePrimaryKey(to)

		def constructor(implicit m: ValuesMap) = new LinkedPeople(from, to, note) with Persisted
	}
}

