package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         24 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToManyCompositeKeySuite extends FunSuite with ShouldMatchers {

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(HouseEntity, DoorEntity))

		// aliases
		val he = HouseEntity
		val de = DoorEntity

		test("batch insert") {
			createTables()
			noise
			noise

			val h1 = House("H1", Set(Door("D1"), Door("D2")))
			val h2 = House("H2", Set(Door("D3"), Door("D4")))

			val List(i1, i2) = mapperDao.insertBatch(HouseEntity, List(h1, h2))
			i1 should be(h1)
			i2 should be(h2)

			mapperDao.select(HouseEntity, (i1.id, i1.address)).get should be(i1)
			mapperDao.select(HouseEntity, (i2.id, i2.address)).get should be(i2)
		}

		test("batch update on inserted") {
			createTables()
			noise
			noise

			val h1 = House("H1", Set(Door("D1"), Door("D2")))
			val h2 = House("H2", Set(Door("D3"), Door("D4")))

			val List(i1, i2) = mapperDao.insertBatch(HouseEntity, List(h1, h2))

			val u1 = i1.copy(doors = i1.doors - Door("D1") + Door("D10"))
			val u2 = i2.copy(doors = i2.doors - Door("D3") + Door("D30"))

			val List(up1, up2) = mapperDao.updateBatch(HouseEntity, List((i1, u1), (i2, u2)))
			up1 should be(u1)
			up2 should be(u2)

			mapperDao.select(HouseEntity, (up1.id, up1.address)).get should be(up1)
			mapperDao.select(HouseEntity, (up2.id, up2.address)).get should be(up2)
		}

		test("batch update on selected") {
			createTables()
			noise
			noise

			val h1 = House("H1", Set(Door("D1"), Door("D2")))
			val h2 = House("H2", Set(Door("D3"), Door("D4")))

			val List(i1, i2) = mapperDao.insertBatch(HouseEntity, List(h1, h2)).map {
				h =>
					mapperDao.select(HouseEntity, (h.id, h.address)).get
			}

			val u1 = i1.copy(doors = i1.doors - Door("D1") + Door("D10"))
			val u2 = i2.copy(doors = i2.doors - Door("D3") + Door("D30"))

			val List(up1, up2) = mapperDao.updateBatch(HouseEntity, List((i1, u1), (i2, u2)))
			up1 should be(u1)
			up2 should be(u2)

			mapperDao.select(HouseEntity, (up1.id, up1.address)).get should be(up1)
			mapperDao.select(HouseEntity, (up2.id, up2.address)).get should be(up2)
		}

		test("query") {
			createTables()

			noise
			noise

			val h1 = House("London", Set(Door("kitchen"), Door("bathroom")))
			val h2 = House("London", Set(Door("balcony"), Door("bathroom")))

			mapperDao.insert(HouseEntity, h1)
			mapperDao.insert(HouseEntity, h2)

			import Query._

			(select
				from he
				where he.address === "London"
				).toSet should be === Set(h1, h2)

			(select
				from he
				join(he, he.doors, de)
				where he.address === "London" and de.location === "balcony"
				).toList should be === List(h2)

			(select
				from he
				join(he, he.doors, de)
				where he.address === "London" and de.location === "bathroom"
				).toSet should be === Set(h1, h2)
		}

		test("insert, select and delete") {
			createTables()

			noise
			noise

			val h = House("London", Set(Door("kitchen"), Door("bathroom")))

			val inserted = mapperDao.insert(HouseEntity, h)
			inserted should be === h

			mapperDao.select(HouseEntity, (inserted.id, h.address)).get should be === inserted
			mapperDao.delete(HouseEntity, inserted)
			mapperDao.select(HouseEntity, (inserted.id, h.address)) should be === None
		}

		test("update, remove") {
			createTables()

			noise
			noise

			val inserted = mapperDao.insert(HouseEntity, House("London", Set(Door("kitchen"), Door("bathroom"))))
			val upd = inserted.copy(doors = inserted.doors.filter(_.location == "kitchen"))
			val updated = mapperDao.update(HouseEntity, inserted, upd)
			updated should be === upd
			val selected = mapperDao.select(HouseEntity, (inserted.id, inserted.address)).get
			selected should be === updated
		}

		test("update, add") {
			createTables()

			noise
			noise

			val inserted = mapperDao.insert(HouseEntity, House("London", Set(Door("kitchen"))))
			val upd = inserted.copy(doors = inserted.doors + Door("bathroom"))
			val updated = mapperDao.update(HouseEntity, inserted, upd)
			updated should be === upd
			val selected = mapperDao.select(HouseEntity, (inserted.id, inserted.address)).get
			selected should be === updated
		}

		def noise = mapperDao.insert(HouseEntity, House("Paris", Set(Door("livingroom"), Door("balcony"))))
		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
			if (Setup.database == "oracle")
				Setup.createSeq(jdbc, "HouseSeq")
		}
	}

	case class House(address: String, doors: Set[Door])

	case class Door(location: String)

	object HouseEntity extends Entity[(Int, String),SurrogateIntAndNaturalStringId, House] {
		val id = key("id") sequence (
			if (database == "oracle") Some("HouseSeq") else None
			) autogenerated (_.id)
		val address = key("address") to (_.address)
		val doors = onetomany(DoorEntity) to (_.doors)

		def constructor(implicit m) = new House(address, doors) with Stored {
			val id = m(HouseEntity.id)
		}
	}

	object DoorEntity extends Entity[String,NaturalStringId, Door] {
		val location = column("location") to (_.location)

		declarePrimaryKey(location)

		def constructor(implicit m) = new Door(location) with Stored
	}

}