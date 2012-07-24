package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 24 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToManyCompositeKeySuite extends FunSuite with ShouldMatchers {

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(HouseEntity, DoorEntity))

		test("insert and select") {
			createTables()

			noise
			noise

			val h = House("London", Set(Door("kitchen"), Door("bathroom")))

			val inserted = mapperDao.insert(HouseEntity, h)
			inserted should be === h

			mapperDao.select(HouseEntity, List(inserted.id, inserted.address)).get should be === inserted
		}

		def noise = mapperDao.insert(HouseEntity, House("Paris", Set(Door("livingroom"), Door("balcony"))))
		def createTables() =
			{
				Setup.dropAllTables(jdbc)
				Setup.queries(this, jdbc).update("ddl")
			}
	}

	case class House(address: String, doors: Set[Door])
	case class Door(location: String)

	object HouseEntity extends Entity[IntId, House] {
		val id = key("id") autogenerated (_.id)
		val address = key("address") to (_.address)
		val doors = onetomany(DoorEntity) to (_.doors)

		def constructor(implicit m) = new House(address, doors) with IntId with Persisted {
			val id: Int = HouseEntity.id
		}
	}

	object DoorEntity extends SimpleEntity[Door] {
		val location = column("location") to (_.location)

		declarePrimaryKey(location)

		def constructor(implicit m) = new Door(location) with Persisted
	}
}