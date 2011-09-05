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
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(UserEntity))

	import mapperDao._

	"insert" in {
		createTables

		val user = User("Some", "Body", 20)
		val inserted = insert(UserEntity, user)
		inserted must_== user
	}

	def createTables = {
		jdbc.update("""drop table if exists "User" cascade""")
		jdbc.update("""
			create table "User" (
				name varchar(20) not null,
				surname varchar(20) not null,
				age int not null,
				primary key (name,surname)
			)
		""")
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
}