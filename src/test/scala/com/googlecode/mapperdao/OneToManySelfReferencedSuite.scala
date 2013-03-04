package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * tests one-to-many references to self
 *
 * @author kostantinos.kougios
 *
 *         5 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToManySelfReferencedSuite extends FunSuite with ShouldMatchers {

	import OneToManySelfReferencedSuite._

	val (jdbc, mapperDao, queryDao) = setup

	test("batch insert") {
		createTables

		val p1 = new Person("P1", Set(new Person("F1", Set()), new Person("F2", Set())))
		val p2 = new Person("P2", Set(new Person("F3", Set()), new Person("F4", Set())))

		val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2))
		i1 should be(p1)
		i2 should be(p2)

		mapperDao.select(PersonEntity, i1.id).get should be(i1)
		mapperDao.select(PersonEntity, i2.id).get should be(i2)
	}

	test("batch update on inserted") {
		createTables

		val f1 = new Person("F1", Set())
		val p1 = new Person("P1", Set(f1, new Person("F2", Set())))
		val f3 = new Person("F3", Set())
		val p2 = new Person("P2", Set(f3, new Person("F4", Set())))

		val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2))
		val u1 = i1.copy(friends = i1.friends - f1 + new Person("F10", Set()))
		val u2 = i2.copy(friends = i2.friends - f3 + new Person("F30", Set()))
		val List(up1, up2) = mapperDao.updateBatch(PersonEntity, List((i1, u1), (i2, u2)))
		up1 should be(u1)
		up2 should be(u2)

		mapperDao.select(PersonEntity, up1.id).get should be(up1)
		mapperDao.select(PersonEntity, up2.id).get should be(up2)
	}

	test("batch update on selected") {
		createTables

		val f1 = new Person("F1", Set())
		val p1 = new Person("P1", Set(f1, new Person("F2", Set())))
		val f3 = new Person("F3", Set())
		val p2 = new Person("P2", Set(f3, new Person("F4", Set())))

		val List(i1, i2) = mapperDao.insertBatch(PersonEntity, List(p1, p2)).map {
			p =>
				mapperDao.select(PersonEntity, p.id).get
		}
		val u1 = i1.copy(friends = i1.friends - f1 + new Person("F10", Set()))
		val u2 = i2.copy(friends = i2.friends - f3 + new Person("F30", Set()))
		val List(up1, up2) = mapperDao.updateBatch(PersonEntity, List((i1, u1), (i2, u2)))
		up1 should be(u1)
		up2 should be(u2)

		mapperDao.select(PersonEntity, up1.id).get should be(up1)
		mapperDao.select(PersonEntity, up2.id).get should be(up2)
	}

	test("insert") {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)
		inserted should be === person
	}

	test("insert and select") {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)
		val selected = mapperDao.select(PersonEntity, inserted.id).get
		selected should be(person)
	}

	test("update, remove from traversable") {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = new Person("main-changed", inserted.friends.filterNot(_.name == "friend1"))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated should be === modified

		mapperDao.select(PersonEntity, updated.id).get should be === updated
	}

	test("update, add to traversable") {
		createTables

		val person = new Person("main-person", Set(new Person("friend1", Set()), new Person("friend2", Set())))
		val inserted = mapperDao.insert(PersonEntity, person)
		val friends = inserted.friends
		val modified = new Person("main-changed", friends + new Person("friend3", Set()))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated should be === modified

		mapperDao.select(PersonEntity, updated.id).get should be === updated
	}

	test("3 levels deep") {
		createTables

		val person = Person("level1", Set(Person("level2-friend1", Set(Person("level3-friend1-1", Set()), Person("level3-friend1-2", Set()))), Person("level2-friend2", Set(Person("level3-friend2-1", Set())))))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = Person("main-changed", inserted.friends + Person("friend3", Set(Person("level3-friend3-1", Set()))))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated should be === modified

		mapperDao.select(PersonEntity, updated.id).get should be === updated
	}

	test("use already persisted friends") {
		val level3 = Set(Person("level3-friend1-1", Set()), Person("level3-friend1-2", Set()))
		val level3Inserted = level3.map(mapperDao.insert(PersonEntity, _)).toSet[Person]
		val person = Person("level1", Set(Person("level2-friend1", level3Inserted), Person("level2-friend2", Set(Person("level3-friend2-1", Set())))))
		val inserted = mapperDao.insert(PersonEntity, person)

		val modified = Person("main-changed", inserted.friends + Person("friend3", Set(Person("level3-friend3-1", Set()))))
		val updated = mapperDao.update(PersonEntity, inserted, modified)
		updated should be === modified

		mapperDao.select(PersonEntity, updated.id).get should be === updated
	}

	def setup = {

		val typeRegistry = TypeRegistry(PersonEntity)

		Setup.setupMapperDao(typeRegistry)
	}

	def createTables {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
		Setup.database match {
			case "oracle" =>
				Setup.createMySeq(jdbc)
			case _ =>
		}
	}
}

object OneToManySelfReferencedSuite {

	case class Person(val name: String, val friends: Set[Person])

	object PersonEntity extends Entity[Int,SurrogateIntId, Person] {
		val aid = key("id") sequence (Setup.database match {
			case "oracle" => Some("myseq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val friends = onetomany(PersonEntity) foreignkey "friend_id" to (_.friends)

		def constructor(implicit m) = new Person(name, friends) with Stored {
			val id: Int = aid
		}
	}

}