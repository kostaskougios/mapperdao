package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class OneToOneWithOneToManySuite extends FunSuite with ShouldMatchers
{
	if (Setup.database == "h2") {
		val (jdbc, mapperDao, _) = Setup.setupMapperDao(List(ProductEntity, InventoryEntity, CatalogEntity))

		test("CR") {
			createTables()
			val catalog = Catalog(
				1,
				List(
					Product(5, Inventory(1)),
					Product(6, null)
				)
			)
			mapperDao.insert(CatalogEntity, catalog)

			mapperDao.select(CatalogEntity, 1).get should be(catalog)
		}

		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
	}

	case class Catalog(id: Int, products: List[Product])

	case class Product(id: Int, inventory: Inventory)

	case class Inventory(stock: Int)

	object InventoryEntity extends Entity[Int, NoId, Inventory]
	{
		val stock = column("stock") to (_.stock)

		def constructor(implicit m: ValuesMap) = new Inventory(stock) with Stored
	}

	object ProductEntity extends Entity[Int, NaturalIntId, Product]
	{
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m: ValuesMap) = new Product(id, inventory) with Stored
	}

	object CatalogEntity extends Entity[Int, NaturalIntId, Catalog]
	{
		val id = key("id") to (_.id)
		val products = onetomany(ProductEntity) to (_.products)

		def constructor(implicit m: ValuesMap) = new Catalog(id, products) with Stored
	}

}
