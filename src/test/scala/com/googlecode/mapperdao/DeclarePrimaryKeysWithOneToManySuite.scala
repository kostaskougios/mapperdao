package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         16 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class DeclarePrimaryKeysWithOneToManySuite extends FunSuite with ShouldMatchers
{
	val LinkedPeopleEntity = PersonEntity.LinkedPeopleEntity
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(PersonEntity, LinkedPeopleEntity))

	val lpe = LinkedPeopleEntity

	def testData = {
		val person1 = Person("person1@m.com", "Person One", Set())
		val person2 = Person("person2@m.com", "Person Two", Set())
		val person3 = Person("person3@m.com", "Person Three", Set())
		val (p1 :: p2 :: p3 :: Nil) = mapperDao.insertBatch(UpdateConfig.default, PersonEntity, person1 :: person2 :: person3 :: Nil)

		val p1u = mapperDao.update(PersonEntity, p1, p1.copy(
			linked = Set(
				LinkedPeople(p2, "good chap this p2"),
				LinkedPeople(p3, "hi p3")
			)
		)
		)

		val p2u = mapperDao.update(PersonEntity, p2, p2.copy(
			linked = Set(LinkedPeople(p3, "I like p3"))
		))

		(p1u, p2u, p3)
	}

	test("query") {
		createTables()
		val (p1, p2, p3) = testData

		import Query._
		(
			select
				from lpe
				where lpe.from === p1
			).toSet(queryDao) should be === Set(
			LinkedPeople(p2, "good chap this p2"),
			LinkedPeople(p3, "hi p3")
		)
		(
			select
				from lpe
				where lpe.from === p2
			).toSet(queryDao) should be === Set(
			LinkedPeople(p3, "I like p3")
		)
	}

	test("rud") {
		createTables()
		val (p1, p2, p3) = testData

		mapperDao.select(LinkedPeopleEntity, (p1, p2)).get should be === LinkedPeople(p2, "good chap this p2")
		mapperDao.select(LinkedPeopleEntity, (p1, p3)).get should be === LinkedPeople(p3, "hi p3")
		val slp2 = mapperDao.select(LinkedPeopleEntity, (p2, p3)).get
		slp2 should be === LinkedPeople(p3, "I like p3")

		mapperDao.update(LinkedPeopleEntity, slp2, LinkedPeople(p1, "now I like p1"))
		val rslp2 = mapperDao.select(LinkedPeopleEntity, (p2, p1)).get
		rslp2 should be === LinkedPeople(p1, "now I like p1")

		mapperDao.delete(LinkedPeopleEntity, rslp2)
		mapperDao.select(LinkedPeopleEntity, (p2, p1)) should be(None)

		mapperDao.select(LinkedPeopleEntity, (p1, p2)).get should be === LinkedPeople(p2, "good chap this p2")
		mapperDao.select(LinkedPeopleEntity, (p1, p3)).get should be === LinkedPeople(p3, "hi p3")
	}

	def createTables() {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Person(email: String, name: String, linked: Set[LinkedPeople])
	{
		// for this test, we match only against email and name
		override def equals(o: Any) = o match {
			case Person(e, n, _) =>
				email == e && name == n
			case _ => false
		}

		override def hashCode = name.hashCode
	}

	case class LinkedPeople(to: Person, note: String)

	object PersonEntity extends Entity[String, NaturalStringId, Person]
	{

		val email = key("email") to (_.email)
		val name = column("name") to (_.name)

		val LinkedPeopleEntity = new LinkedPeopleEntityDecl(this)
		// avoid the cyclic stack overflow
		val linked = onetomany(LinkedPeopleEntity) foreignkey ("from_id") to (_.linked)

		def constructor(implicit m: ValuesMap) =
			new Person(email, name, linked) with Stored
	}

	class LinkedPeopleEntityDecl(pe: PersonEntity.type)
		extends Entity[(Person with NaturalStringId, Person with NaturalStringId), With2Ids[Person with NaturalStringId, Person with NaturalStringId], LinkedPeople]
	{

		val to = manytoone(pe) foreignkey ("to_id") to (_.to)
		val note = column("note") to (_.note)

		val from = declarePrimaryKey(PersonEntity.linked)
		declarePrimaryKey(to)

		def constructor(implicit m: ValuesMap) =
			new LinkedPeople(to, note) with Stored
	}

}

