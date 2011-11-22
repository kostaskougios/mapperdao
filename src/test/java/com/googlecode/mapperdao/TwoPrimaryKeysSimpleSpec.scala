package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 5 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class TwoPrimaryKeysSimpleSpec extends SpecificationWithJUnit {
	import TwoPrimaryKeysSimpleSpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(UserEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

	"insert" in {
		createTables

		val user = User("Some", "Body", 20)
		val inserted = insert(UserEntity, user)
		inserted must_== user
	}

	"select" in {
		createTables

		val u1 = insert(UserEntity, User("Some", "Body", 20))
		val u2 = insert(UserEntity, User("An", "Other", 25))

		select(UserEntity, "Some", "Body").get must_== u1
		select(UserEntity, "An", "Other").get must_== u2
	}

	"update" in {
		createTables

		val iu1 = insert(UserEntity, User("Some", "Body", 20))
		val iu2 = insert(UserEntity, User("An", "Other", 25))

		val u1updated = User("SomeX", "BodyX", 21)
		val uu1 = update(UserEntity, iu1, u1updated)
		uu1 must_== u1updated
		val u2updated = User("AnX", "OtherX", 26)
		val uu2 = update(UserEntity, iu2, u2updated)
		uu2 must_== u2updated

		select(UserEntity, "SomeX", "BodyX").get must_== uu1
		select(UserEntity, "AnX", "OtherX").get must_== uu2

		select(UserEntity, "Some", "Body") must beNone
		select(UserEntity, "An", "Other") must beNone
	}

	"delete" in {
		createTables
		insert(UserEntity, User("A", "B", 25))
		val inserted = insert(UserEntity, User("Some", "Body", 20))
		delete(UserEntity, inserted)
		select(UserEntity, "Some", "Body") must beNone
		select(UserEntity, "A", "B").get must_== User("A", "B", 25)
	}

	"query" in {
		createTables
		val u0 = insert(UserEntity, User("Kostas", "Kougios", 20))
		val u1 = insert(UserEntity, User("Ajax", "Perseus", 21))
		val u2 = insert(UserEntity, User("Leonidas", "Kougios", 22))
		val u3 = insert(UserEntity, User("Antonis", "Agnostos", 23))
		val u4 = insert(UserEntity, User("Kostas", "Patroklos", 24))

		query(q0) must_== List(u0, u4, u2)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object TwoPrimaryKeysSimpleSpec {

	case class User(val name: String, val surname: String, val age: Int)

	object UserEntity extends SimpleEntity[User](classOf[User]) {
		val name = key("name") to (_.name)
		val surname = key("surname") to (_.surname)
		val age = column("age") to (_.age)

		def constructor(implicit m) = new User(name, surname, age) with Persisted
	}

	object TestQueries {
		val u = UserEntity

		import Query._
		def q0 = select from u where
			u.surname === "Kougios" or u.name === "Kostas" orderBy (u.name, u.surname)
	}

}