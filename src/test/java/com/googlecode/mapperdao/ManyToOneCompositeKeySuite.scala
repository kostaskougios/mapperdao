package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 26 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToOneCompositeKeySuite extends FunSuite with ShouldMatchers {

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(CityEntity, HouseEntity))

		// aliases
		val ce = CityEntity
		val he = HouseEntity

		test("query") {
			createTables()
			val city1 = mapperDao.insert(CityEntity, City("LDN", "London"))
			val city2 = mapperDao.insert(CityEntity, City("ATH", "Athens"))
			val h1i = mapperDao.insert(HouseEntity, House("Putney", city1))
			val h2i = mapperDao.insert(HouseEntity, House("Greenwitch", city1))
			val h3i = mapperDao.insert(HouseEntity, House("Holargos", city2))
			val h4i = mapperDao.insert(HouseEntity, House("Pagrati", city2))

			import Query._

			(select
				from he
				where he.address === "Putney"
			).toSet should be === Set(h1i)

			(select
				from he
				join (he, he.city, ce)
				where ce.name === "Athens"
			).toSet should be === Set(h3i, h4i)

			(select
				from he
				join (he, he.city, ce)
				where ce.name === "London"
			).toSet should be === Set(h1i, h2i)

			(select
				from he
				join (he, he.city, ce)
				where ce.name === "Athens" and ce.reference === "ATH"
			).toSet should be === Set(h3i, h4i)
		}

		test("insert, select and delete") {
			createTables()
			val city = mapperDao.insert(CityEntity, City("LDN", "London"))
			val h1 = House("Putney", city)
			val h2 = House("Greenwitch", city)
			val h1i = mapperDao.insert(HouseEntity, h1)
			h1i should be === h1
			val h2i = mapperDao.insert(HouseEntity, h2)
			h2i should be === h2

			mapperDao.select(HouseEntity, h1i.id).get should be === h1i
			mapperDao.select(HouseEntity, h2i.id).get should be === h2i

			mapperDao.delete(HouseEntity, h1i.id)
			mapperDao.select(HouseEntity, h1i.id) should be === None
			mapperDao.select(HouseEntity, h2i.id).get should be === h2i
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
			updated1 should be === upd1

			mapperDao.select(HouseEntity, h1i.id).get should be === updated1
			mapperDao.select(HouseEntity, h2i.id).get should be === h2i
		}

		def createTables() =
			{
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

	object CityEntity extends Entity[IntId, City] {
		val id = key("id") sequence (
			if (database == "oracle") Some("CitySeq") else None
		) autogenerated (_.id)
		val reference = key("reference") to (_.reference)
		val name = column("name") to (_.name)

		def constructor(implicit m) = new City(reference, name) with IntId with Persisted {
			val id: Int = CityEntity.id
		}
	}

	object HouseEntity extends Entity[IntId, House] {
		val id = key("id") sequence (
			if (database == "oracle") Some("HouseSeq") else None
		) autogenerated (_.id)
		val address = column("address") to (_.address)
		val city = manytoone(CityEntity) to (_.city)

		def constructor(implicit m) = new House(address, city) with IntId with Persisted {
			val id: Int = HouseEntity.id
		}
	}
}