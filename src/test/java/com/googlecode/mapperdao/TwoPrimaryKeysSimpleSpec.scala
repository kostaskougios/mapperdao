package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 5 Sep 2011
 */
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
		Setup.database match {
			case "postgresql" =>
				jdbc.update("""drop table if exists "User" cascade""")
				jdbc.update("""
			create table "User" (
				name varchar(20) not null,
				surname varchar(20) not null,
				age int not null,
				primary key (name,surname)
			)
		""")
			case "mysql" =>
				jdbc.update("""drop table if exists User cascade""")
				jdbc.update("""
			create table User (
				name varchar(20) not null,
				surname varchar(20) not null,
				age int not null,
				primary key (name,surname)
			)
		""")
		}
	}
}

object TwoPrimaryKeysSimpleSpec {

	case class User(val name: String, val surname: String, val age: Int)

	object UserEntity extends SimpleEntity[User](classOf[User]) {
		val name = stringPK("name", _.name)
		val surname = stringPK("surname", _.surname)
		val age = int("age", _.age)

		val constructor = (m: ValuesMap) => new User(m(name), m(surname), m(age)) with Persisted {
			val valuesMap = m
		}
	}

	object TestQueries {
		val u = UserEntity

		import Query._
		def q0 = select from u where
			u.surname === "Kougios" or u.name === "Kostas" orderBy (u.name, u.surname)
	}

}