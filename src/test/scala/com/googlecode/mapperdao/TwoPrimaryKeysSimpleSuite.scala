package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 5 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class TwoPrimaryKeysSimpleSuite extends FunSuite with ShouldMatchers {
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(UserEntity))

	test("insert") {
		createTables

		val user = User("Some", "Body", 20)
		val inserted = mapperDao.insert(UserEntity, user)
		inserted should be === user
	}

	test("select") {
		createTables

		val u1 = mapperDao.insert(UserEntity, User("Some", "Body", 20))
		val u2 = mapperDao.insert(UserEntity, User("An", "Other", 25))

		mapperDao.select(UserEntity, "Some", "Body").get should be === u1
		mapperDao.select(UserEntity, "An", "Other").get should be === u2
	}

	test("update") {
		createTables

		val iu1 = mapperDao.insert(UserEntity, User("Some", "Body", 20))
		val iu2 = mapperDao.insert(UserEntity, User("An", "Other", 25))

		val u1updated = User("SomeX", "BodyX", 21)
		val uu1 = mapperDao.update(UserEntity, iu1, u1updated)
		uu1 should be === u1updated
		val u2updated = User("AnX", "OtherX", 26)
		val uu2 = mapperDao.update(UserEntity, iu2, u2updated)
		uu2 should be === u2updated

		mapperDao.select(UserEntity, "SomeX", "BodyX").get should be === uu1
		mapperDao.select(UserEntity, "AnX", "OtherX").get should be === uu2

		mapperDao.select(UserEntity, "Some", "Body") should be(None)
		mapperDao.select(UserEntity, "An", "Other") should be(None)
	}

	test("delete") {
		createTables
		mapperDao.insert(UserEntity, User("A", "B", 25))
		val inserted = mapperDao.insert(UserEntity, User("Some", "Body", 20))
		mapperDao.delete(UserEntity, inserted)
		mapperDao.select(UserEntity, "Some", "Body") should be(None)
		mapperDao.select(UserEntity, "A", "B").get should be === User("A", "B", 25)
	}

	test("query") {
		createTables
		val u0 = mapperDao.insert(UserEntity, User("Kostas", "Kougios", 20))
		val u1 = mapperDao.insert(UserEntity, User("Ajax", "Perseus", 21))
		val u2 = mapperDao.insert(UserEntity, User("Leonidas", "Kougios", 22))
		val u3 = mapperDao.insert(UserEntity, User("Antonis", "Agnostos", 23))
		val u4 = mapperDao.insert(UserEntity, User("Kostas", "Patroklos", 24))

		val u = UserEntity

		import Query._
		queryDao.query(select from u where
			u.surname === "Kougios" or u.name === "Kostas" orderBy (u.name, u.surname)) should be === List(u0, u4, u2)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class User(val name: String, val surname: String, val age: Int)

	object UserEntity extends Entity[NaturalStringAndStringIds, User] {
		val name = key("name") to (_.name)
		val surname = key("surname") to (_.surname)
		val age = column("age") to (_.age)

		def constructor(implicit m) = new User(name, surname, age) with NaturalStringAndStringIds
	}
}
