package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.jdbc.Jdbc
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

/**
 * @author kostantinos.kougios
 *
 *         18 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class UpdateConfigSuite extends FunSuite with Matchers
{

	test("one-to-many update.deleteConfig") {
		import UpdateConfigSuiteOneToManyDecl._
		val (jdbc, mapperDao, _) = Setup.setupMapperDao(List(FloorEntity, HouseEntity, PersonEntity))
		prepareDb(jdbc, "OneToManyDecl")

		val inserted = mapperDao.insert(PersonEntity, Person(1, "kostas", Set(House(10, Set(Floor(5, "floor5"), Floor(6, "floor6"))), House(11, Set(Floor(7, "floor7"), Floor(8, "floor8"))))))
		mapperDao.update(UpdateConfig(deleteConfig = DeleteConfig(propagate = true)), PersonEntity, inserted, Person(inserted.id, inserted.name, inserted.owns.filterNot(_.id == 11)))

		jdbc.queryForInt("select count(*) from Floor") should be === 2
	}

	def prepareDb(jdbc: Jdbc, tableCreationScript: String) {
		Setup.dropAllTables(jdbc)
		val queries = Setup.queries(this, jdbc)
		queries.update(tableCreationScript)
	}
}

object UpdateConfigSuiteOneToManyDecl
{

	case class Person(id: Int, var name: String, owns: Set[House])

	case class House(id: Int, floors: Set[Floor])

	case class Floor(id: Int, description: String)

	object FloorEntity extends Entity[Int, NaturalIntId, Floor]
	{
		val id = key("id") to (_.id)
		val description = column("description") to (_.description)

		def constructor(implicit m) = new Floor(id, description) with Stored
	}

	object HouseEntity extends Entity[Int, NaturalIntId, House]
	{
		val id = key("id") to (_.id)
		val floors = onetomany(FloorEntity) to (_.floors)

		def constructor(implicit m) = new House(id, floors) with Stored
	}

	object PersonEntity extends Entity[Int, NaturalIntId, Person]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val houses = onetomany(HouseEntity) to (_.owns)

		def constructor(implicit m) = new Person(id, name, houses) with Stored
	}

}
