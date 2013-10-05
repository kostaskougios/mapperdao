package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         15 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class DeclarePrimaryKeysWithManyToOneSuite extends FunSuite with Matchers
{
	val LinkedPeopleEntity = PersonEntity.LinkedPeopleEntity
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(PersonEntity, LinkedPeopleEntity))

	val person1 = Person("person1@example.com", "Mr Person One")
	val person2 = Person("person2@example.com", "Mrs Person Two")
	val person3 = Person("person3@example.com", "Mr Person Three")
	val person4 = Person("person4@example.com", "Mr Person Four")

	test("crud on PersonEntity") {
		createTables()
		val p1 = mapperDao.insert(PersonEntity, person1)
		val p2 = mapperDao.insert(PersonEntity, person2)
		val p3 = mapperDao.insert(PersonEntity, person3)
		val p4 = mapperDao.insert(PersonEntity, person4)

		// noise
		val p2updated = mapperDao.update(PersonEntity, p2,
			p2.copy(
				linked = Set(
					LinkedPeople(p2, p1, "p2 likes p1")
				),
				linkedToMe = Set(
					LinkedPeople(p4, p2, "p4 likes p2")
				)
			)
		)

		val upd = p3.copy(
			linked = Set(
				LinkedPeople(p3, p2, "p3 likes p2")
			),
			linkedToMe = Set(
				LinkedPeople(p4, p3, "p4 likes p3")
			)
		)
		val updated = mapperDao.update(PersonEntity, p3, upd)
		updated should be === upd

		val selected = mapperDao.select(PersonEntity, person3.email).get
		selected should be === updated
		selected.linked should be === updated.linked // please see Person.equals as why this is necessary
		selected.linkedToMe should be === updated.linkedToMe

		mapperDao.delete(PersonEntity, selected)

		val p2selected = mapperDao.select(PersonEntity, person2.email).get
		p2selected should be === p2updated
		p2selected.linked should be === p2updated.linked
		p2selected.linkedToMe should be === p2updated.linkedToMe
	}

	test("crud straight on LinkedPeopleEntity") {
		createTables()

		val p1 = mapperDao.insert(PersonEntity, person1)
		val p2 = mapperDao.insert(PersonEntity, person2)
		val p3 = mapperDao.insert(PersonEntity, person3)

		val lp1 = LinkedPeople(p1, p2, "these like each other")
		val lp1Inserted = mapperDao.insert(LinkedPeopleEntity, lp1)
		lp1Inserted should be === lp1

		// add some noise
		val lp2Inserted = mapperDao.insert(LinkedPeopleEntity, LinkedPeople(p2, p3, "p2 likes p3"))

		val selected = mapperDao.select(LinkedPeopleEntity, (p1, p2)).get
		selected should be === lp1Inserted

		val upd = selected.copy(from = p3, note = "now p3 likes p2")
		val updated = mapperDao.update(LinkedPeopleEntity, selected, upd)
		updated should be === upd

		// verify the update
		mapperDao.select(LinkedPeopleEntity, (p1, p2)) should be(None)
		val reselected = mapperDao.select(LinkedPeopleEntity, (p3, p2)).get
		reselected should be === updated

		// now delete
		mapperDao.delete(LinkedPeopleEntity, reselected)
		mapperDao.select(LinkedPeopleEntity, (p3, p2)) should be(None)
		mapperDao.select(LinkedPeopleEntity, (p2, p3)).get should be === lp2Inserted
	}

	test("queries") {
		createTables()

		val p1 = mapperDao.insert(PersonEntity, person1)
		val p2 = mapperDao.insert(PersonEntity, person2)
		val p3 = mapperDao.insert(PersonEntity, person3)

		val lp1 = mapperDao.insert(LinkedPeopleEntity, LinkedPeople(p1, p2, "these like each other"))
		val lp2 = mapperDao.insert(LinkedPeopleEntity, LinkedPeople(p2, p3, "p2 likes p3"))
		val lp3 = mapperDao.insert(LinkedPeopleEntity, LinkedPeople(p1, p3, "p1 likes p3 too"))

		import Query._
		val lpe = LinkedPeopleEntity
		(
			select
				from lpe
				where lpe.from === p1
			).toSet(queryDao) should be === Set(lp1, lp3)
		(
			select
				from lpe
				where lpe.to === p3
			).toSet(queryDao) should be === Set(lp2, lp3)

		(
			select
				from lpe
				where lpe.from === p1 and lpe.to === p3
			).toSet(queryDao) should be === Set(lp3)

	}

	def createTables() {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Person(
		email: String, name: String,
		linked: Set[LinkedPeople] = Set(),
		linkedToMe: Set[LinkedPeople] = Set()
		)
	{
		// for this test, we match only against email and name
		override def equals(o: Any) = o match {
			case Person(e, n, _, _) =>
				email == e && name == n
			case _ => false
		}

		override def hashCode = name.hashCode
	}

	case class LinkedPeople(from: Person, to: Person, note: String)

	object PersonEntity extends Entity[String, NaturalStringId, Person]
	{

		val email = key("email") to (_.email)
		val name = column("name") to (_.name)

		val LinkedPeopleEntity = new LinkedPeopleEntityDecl(this)
		// avoid the cyclic stack overflow
		val linked = onetomany(LinkedPeopleEntity) foreignkey ("from_id") to (_.linked)
		val linkedToMe = onetomany(LinkedPeopleEntity) foreignkey ("to_id") to (_.linkedToMe)

		def constructor(implicit m: ValuesMap) = {
			new Person(email, name, linked, linkedToMe) with Stored
		}
	}

	class LinkedPeopleEntityDecl(pe: PersonEntity.type)
		extends Entity[(Person with NaturalStringId, Person with NaturalStringId), With2Ids[Person with NaturalStringId, Person with NaturalStringId], LinkedPeople]
	{
		val from = manytoone(pe) foreignkey ("from_id") to (_.from)
		val to = manytoone(pe) foreignkey ("to_id") to (_.to)
		val note = column("note") to (_.note)

		declarePrimaryKey(from)
		declarePrimaryKey(to)

		def constructor(implicit m: ValuesMap) = new LinkedPeople(from, to, note) with Stored
	}

}

