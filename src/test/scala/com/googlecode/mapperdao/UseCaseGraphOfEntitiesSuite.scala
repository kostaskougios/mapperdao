package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author: kostas.kougios
 *          Date: 09/04/13
 */
@RunWith(classOf[JUnitRunner])
class UseCaseGraphOfEntitiesSuite extends FunSuite with ShouldMatchers
{

	import UseCaseGraphOfEntitiesSuite._

	if (Setup.database == "postgresql") {
		val (jdbc, mapperDao, _) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

		test("insert") {
			createTables()
			val p1 = Product(
				"p1", Set(
					Attribute("a1", "v1",
						Set(
							Log("attr1 first insert")
						)
					)),
				Set(
					Log("prod1 first insert")
				)
			)
			val i1 = mapperDao.insert(ProductEntity, p1)
			i1 should be(p1)

			mapperDao.select(ProductEntity, i1.id).get should be(i1)
		}

		test("insert, shared pre-inserted log") {
			createTables()
			val l1 = Log("common log entry")
			val p1 = Product("p1", Set(
				Attribute("a1", "v1", Set(l1)),
				Attribute("a2", "v2", Set(l1))
			), Set(l1))
			val i1 = mapperDao.insert(ProductEntity, p1)
			i1 should be(p1)

			mapperDao.select(ProductEntity, i1.id).get should be(i1)
		}

		test("insert, shared 2 pre-inserted log") {
			createTables()
			val l1 = Log("Log 1")
			val l2 = Log("Log 2")

			val p1 = Product("p1", Set(
				Attribute("a1", "v1", Set(l1)),
				Attribute("a2", "v2", Set(l1, l2))
			), Set(l1, l2))
			val i1 = mapperDao.insert(ProductEntity, p1)
			i1 should be(p1)

			mapperDao.select(ProductEntity, i1.id).get should be(i1)
		}

		test("insert batch") {
			createTables()
			val p1 = Product(
				"p1", Set(
					Attribute("a1", "v1",
						Set(
							Log("attr1 first insert")
						)
					)),
				Set(
					Log("prod1 first insert")
				)
			)

			val p2 = Product(
				"p2", Set(
					Attribute("a2", "v2",
						Set(
							Log("attr2 first insert"),
							Log("attr2 is now ready")
						)
					),
					Attribute("a3", "v3",
						Set(
							Log("attr3 first insert"),
							Log("attr3 is now ready")
						)
					)
				),
				Set(
					Log("prod1 first insert")
				)
			)

			val List(i1, i2) = mapperDao.insertBatch(ProductEntity, List(p1, p2))
			i1 should be(p1)
			i2 should be(p2)

			mapperDao.select(ProductEntity, i1.id).get should be(i1)
			mapperDao.select(ProductEntity, i2.id).get should be(i2)
		}

		def createTables() {
			Setup.dropAllTables(jdbc)
			val queries = Setup.queries(this, jdbc)
			queries.update("ddl")
		}
	}
}

object UseCaseGraphOfEntitiesSuite
{

	case class Product(name: String, attributes: Set[Attribute], logs: Set[Log])

	case class Attribute(name: String, value: String, logs: Set[Log])

	case class Log(change: String)

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)
		val logs = manytomany(LogEntity) to (_.logs)

		def constructor(implicit m: ValuesMap) = new Product(name, attributes, logs) with Stored
		{
			val id: Int = ProductEntity.id
		}
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute]
	{
		val id = key("id") autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)
		val logs = manytomany(LogEntity) to (_.logs)

		def constructor(implicit m: ValuesMap) = new Attribute(name, value, logs) with Stored
		{
			val id: Int = AttributeEntity.id
		}
	}

	object LogEntity extends Entity[Int, SurrogateIntId, Log]
	{
		val id = key("id") autogenerated (_.id)
		val change = column("change") to (_.change)

		def constructor(implicit m: ValuesMap) = new Log(change) with Stored
		{
			val id: Int = LogEntity.id
		}
	}

}