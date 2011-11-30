package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.jdbc.Queries
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 18 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class UpdateConfigSpec extends SpecificationWithJUnit {

	"one-to-many update.deleteConfig" in {
		import UpdateConfigSpecOneToManyDecl._
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(FloorEntity, HouseEntity, PersonEntity))
		prepareDb(jdbc, "OneToManyDecl")

		val inserted = mapperDao.insert(PersonEntity, Person(1, "kostas", Set(House(10, Set(Floor(5, "nice floor"), Floor(6, "top floor"))), House(11, Set(Floor(7, "nice floor"), Floor(8, "top floor"))))))
		mapperDao.update(UpdateConfig(deleteConfig = DeleteConfig(propagate = true)), PersonEntity, inserted, Person(inserted.id, inserted.name, inserted.owns.filterNot(_.id == 11)))

		jdbc.queryForInt("select count(*) from Floor") must_== 2
	}

	def prepareDb(jdbc: Jdbc, tableCreationScript: String): Unit = {
		Setup.dropAllTables(jdbc)
		val queries = Setup.queries(this, jdbc)
		queries.update(tableCreationScript)
	}
}

object UpdateConfigSpecOneToManyDecl {
	case class Person(val id: Int, var name: String, owns: Set[House])
	case class House(val id: Int, val floors: Set[Floor])
	case class Floor(val id: Int, val description: String)

	object FloorEntity extends SimpleEntity(classOf[Floor]) {
		val id = key("id") to (_.id)
		val description = column("description") to (_.description)

		def constructor(implicit m) = new Floor(id, description) with Persisted
	}

	object HouseEntity extends SimpleEntity(classOf[House]) {
		val id = key("id") to (_.id)
		val floors = onetomany(FloorEntity) to (_.floors)

		def constructor(implicit m) = new House(id, floors) with Persisted
	}

	object PersonEntity extends SimpleEntity(classOf[Person]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val houses = onetomany(HouseEntity) to (_.owns)
		def constructor(implicit m) = new Person(id, name, houses) with Persisted
	}
}
