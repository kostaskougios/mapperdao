package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         26 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneCompositeKeySuite extends FunSuite
{

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(CityEntity, HouseEntity))

		// aliases
		val ce = CityEntity
		val he = HouseEntity

		test("batch insert, new one-part") {
			createTables()
			val c1 = City("C1", "City1")
			val c2 = City("C2", "City2")

			val h1 = House("h1a", c1)
			val h2 = House("h2a", c2)
			val h3 = House("h3a", c2)

			val List(i1, i2, i3) = mapperDao.insertBatch(HouseEntity, List(h1, h2, h3))
			i1 should be(h1)
			i2 should be(h2)
			i3 should be(h3)

			mapperDao.select(HouseEntity, i1.id).get should be(i1)
			mapperDao.select(HouseEntity, i2.id).get should be(i2)
			mapperDao.select(HouseEntity, i3.id).get should be(i3)
		}

		test("batch insert, existing one-part") {
			createTables()
			val List(c1, c2) = mapperDao.insertBatch(CityEntity, List(City("C1", "City1"), City("C2", "City2")))

			val h1 = House("h1a", c1)
			val h2 = House("h2a", c2)
			val h3 = House("h3a", c2)

			val List(i1, i2, i3) = mapperDao.insertBatch(HouseEntity, List(h1, h2, h3))

			mapperDao.select(HouseEntity, i1.id).get should be(i1)
			mapperDao.select(HouseEntity, i2.id).get should be(i2)
			mapperDao.select(HouseEntity, i3.id).get should be(i3)
		}

		test("batch update on inserted") {
			createTables()
			val List(c1, c2) = mapperDao.insertBatch(CityEntity, List(City("C1", "City1"), City("C2", "City2")))

			val h1 = House("h1a", c1)
			val h2 = House("h2a", c2)
			val h3 = House("h3a", c2)

			val List(i1, i2, i3) = mapperDao.insertBatch(HouseEntity, List(h1, h2, h3))

			val u1 = i1.copy(city = c2)
			val u2 = i2.copy(city = c1)
			val u3 = i3.copy(city = c1)

			val List(up1, up2, up3) = mapperDao.updateBatch(HouseEntity, List((i1, u1), (i2, u2), (i3, u3)))

			up1 should be(u1)
			up2 should be(u2)
			up3 should be(u3)

			mapperDao.select(HouseEntity, i1.id).get should be(up1)
			mapperDao.select(HouseEntity, i2.id).get should be(up2)
			mapperDao.select(HouseEntity, i3.id).get should be(up3)
		}

		test("batch update on selected") {
			createTables()
			val List(c1, c2) = mapperDao.insertBatch(CityEntity, List(City("C1", "City1"), City("C2", "City2")))

			val h1 = House("h1a", c1)
			val h2 = House("h2a", c2)
			val h3 = House("h3a", c2)

			val List(i1, i2, i3) = mapperDao.insertBatch(HouseEntity, List(h1, h2, h3))

			val u1 = i1.copy(city = c2)
			val u2 = i2.copy(city = c1)
			val u3 = i3.copy(city = c1)

			val List(up1, up2, up3) = mapperDao.updateBatch(HouseEntity, List((i1, u1), (i2, u2), (i3, u3))).map {
				h =>
					mapperDao.select(HouseEntity, h.id).get
			}

			up1 should be(u1)
			up2 should be(u2)
			up3 should be(u3)

			mapperDao.select(HouseEntity, i1.id).get should be(up1)
			mapperDao.select(HouseEntity, i2.id).get should be(up2)
			mapperDao.select(HouseEntity, i3.id).get should be(up3)
		}

		test("query") {
			createTables()
			val city1 = mapperDao.insert(CityEntity, City("LDN", "London"))
			val city2 = mapperDao.insert(CityEntity, City("ATH", "Athens"))
			val h1i = mapperDao.insert(HouseEntity, House("Putney", city1))
			val h2i = mapperDao.insert(HouseEntity, House("Greenwitch", city1))
			val h3i = mapperDao.insert(HouseEntity, House("Holargos", city2))
			val h4i = mapperDao.insert(HouseEntity, House("Pagrati", city2))

			import com.googlecode.mapperdao.Query._

			(select
				from he
				where he.address === "Putney"
				).toSet should be(Set(h1i))

			(select
				from he
				join(he, he.city, ce)
				where ce.name === "Athens"
				).toSet should be(Set(h3i, h4i))

			(select
				from he
				join(he, he.city, ce)
				where ce.name === "London"
				).toSet should be(Set(h1i, h2i))

			(select
				from he
				join(he, he.city, ce)
				where ce.name === "Athens" and ce.reference === "ATH"
				).toSet should be(Set(h3i, h4i))
		}

		test("insert, select and delete") {
			createTables()
			val city = mapperDao.insert(CityEntity, City("LDN", "London"))
			val h1 = House("Putney", city)
			val h2 = House("Greenwitch", city)
			val h1i = mapperDao.insert(HouseEntity, h1)
			h1i should be(h1)
			val h2i = mapperDao.insert(HouseEntity, h2)
			h2i should be(h2)

			mapperDao.select(HouseEntity, h1i.id).get should be(h1i)
			mapperDao.select(HouseEntity, h2i.id).get should be(h2i)

			mapperDao.delete(HouseEntity, h1i.id)
			mapperDao.select(HouseEntity, h1i.id) should be(None)
			mapperDao.select(HouseEntity, h2i.id).get should be(h2i)
		}

		test("update") {
			createTables()
			val city1 = mapperDao.insert(CityEntity, City("LDN", "London"))
			val city2 = mapperDao.insert(CityEntity, City("ATH", "Athens"))
			val h1 = House("Putney", city1)
			val h2 = House("Greenwitch", city1)
			val h1i = mapperDao.insert(HouseEntity, h1)
			val h2i = mapperDao.insert(HouseEntity, h2)

			val upd1 = h1.copy(city = city2)
			val updated1 = mapperDao.update(HouseEntity, h1i, upd1)
			updated1 should be(upd1)

			mapperDao.select(HouseEntity, h1i.id).get should be(updated1)
			mapperDao.select(HouseEntity, h2i.id).get should be(h2i)
		}

		def createTables() {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
			if (Setup.database == "oracle") {
				Setup.createSeq(jdbc, "CitySeq")
				Setup.createSeq(jdbc, "HouseSeq")
			}
		}
	}

	case class House(address: String, city: City)

	case class City(reference: String, name: String)

	object CityEntity extends Entity[(Int, String), SurrogateIntAndNaturalStringId, City]
	{
		val id = key("id") sequence (
			if (database == "oracle") Some("CitySeq") else None
			) autogenerated (_.id)
		val reference = key("reference") to (_.reference)
		val name = column("name") to (_.name)

		def constructor(implicit m: ValuesMap) = new City(reference, name) with Stored
		{
			val id = m(CityEntity.id)
		}
	}

	object HouseEntity extends Entity[Int, SurrogateIntId, House]
	{
		val id = key("id") sequence (
			if (database == "oracle") Some("HouseSeq") else None
			) autogenerated (_.id)
		val address = column("address") to (_.address)
		val city = manytoone(CityEntity) to (_.city)

		def constructor(implicit m: ValuesMap) = new House(address, city) with Stored
		{
			val id: Int = HouseEntity.id
		}
	}

}