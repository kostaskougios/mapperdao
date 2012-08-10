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
 * 10 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class UseCasePersonAndRolesSuite extends FunSuite with ShouldMatchers {

	if (Setup.database == "postgresql") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(RoleTypeEntity, PersonEntity, SinglePartyRoleEntity))

		val roleType1 = RoleType("Scala Developer", Some("A Scala Software Developer"))
		val roleType2 = RoleType("Java Developer", Some("A Java Software Developer"))
		val roleType3 = RoleType("Groovy Developer", Some("A Groovy Software Developer"))

		val from = DateTime.now
		val to = DateTime.now.plusDays(1)

		test("crud") {
			createTables()

			val role1 = mapperDao.insert(RoleTypeEntity, roleType1)
			val role2 = mapperDao.insert(RoleTypeEntity, roleType2)
			val role3 = mapperDao.insert(RoleTypeEntity, roleType3)

			val person1 = Person("kostas.kougios", "kostas", "kougios", Set(
				SinglePartyRole(
					role1,
					Some(from),
					Some(to)
				),
				SinglePartyRole(
					role2,
					Some(from),
					None
				)
			))

			val inserted1 = mapperDao.insert(PersonEntity, person1)
			inserted1 should be === person1

			// noise
			mapperDao.insert(
				PersonEntity,
				Person("some.other", "some", "other", Set(
					SinglePartyRole(
						role1,
						None,
						None
					),
					SinglePartyRole(
						role3,
						None,
						Some(from)
					)
				))
			)
			val selected1 = mapperDao.select(PersonEntity, inserted1.id).get
			selected1 should be === inserted1

			val updated1 = mapperDao.update(
				PersonEntity,
				selected1,
				Person("kostas.kougios", "kostas", "updated",
					selected1.singlePartyRoles.filter(_.roleType == role2) +
						SinglePartyRole(
							role3,
							Some(from),
							Some(to)
						)
				)
			)
			val selectedAgain1 = mapperDao.select(PersonEntity, inserted1.id).get
			selectedAgain1 should be === updated1
		}

		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	}
	case class Person(id: String, firstName: String, lastName: String, singlePartyRoles: Set[SinglePartyRole])
	case class SinglePartyRole(roleType: RoleType, fromDate: Option[DateTime], toDate: Option[DateTime])
	case class RoleType(name: String, description: Option[String])

	object RoleTypeEntity extends SimpleEntity[RoleType] {
		val name = key("name") to (_.name)
		val description = column("description") option (_.description)

		def constructor(implicit m: ValuesMap) = {
			new RoleType(name, description) with Persisted
		}
	}

	object PersonEntity extends SimpleEntity[Person] {
		val id = key("id") to (_.id)
		val firstName = column("firstname") to (_.firstName)
		val lastName = column("lastname") to (_.lastName)
		val singlePartyRoles = onetomany(SinglePartyRoleEntity) to (_.singlePartyRoles)

		def constructor(implicit m: ValuesMap) = {
			new Person(id, firstName, lastName, singlePartyRoles) with Persisted
		}
	}

	object SinglePartyRoleEntity extends SimpleEntity[SinglePartyRole] {
		val roleType = manytoone(RoleTypeEntity) to (_.roleType)
		val fromDate = column("fromDate") option (_.fromDate)
		val toDate = column("toDate") option (_.toDate)

		declarePrimaryKey(roleType)

		def constructor(implicit m: ValuesMap) = new SinglePartyRole(roleType, fromDate, toDate) with Persisted
	}

}

