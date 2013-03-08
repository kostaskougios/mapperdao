package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 *         1 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneWithoutReverseSuite extends FunSuite with ShouldMatchers
{
	implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	val p = ProductEntity
	val i = InventoryEntity

	test("update from null to existing value ") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = mapperDao.update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		val reUdated = mapperDao.update(InventoryEntity, updated, Inventory(10, mapperDao.select(ProductEntity, 1).get, 8))
		reUdated.product should be === Product(1)
		mapperDao.select(InventoryEntity, 10).get should be === reUdated
	}

	test("query on product") {
		createTables

		import Query._
		val inventories = for (i <- 0 to 10) yield mapperDao.insert(InventoryEntity, Inventory(10 + i, Product(1 + i), 5 + i))
		val p2 = Helpers.asSurrogateIntId(inventories(2).product)
		(
			select
				from i
				where i.product === p2
			).toSet should be === Set(inventories(2))
	}

	test("update to null") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = mapperDao.update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		mapperDao.select(InventoryEntity, 10).get should be === updated
		mapperDao.select(ProductEntity, 1).get should be === Product(1)
	}

	if (Setup.database != "derby") {
		test("update id of related") {
			createTables
			val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
			val updatedProduct = mapperDao.update(ProductEntity, Helpers.asSurrogateIntId(inserted.product), Product(7))
			updatedProduct should be === Product(7)
			mapperDao.select(InventoryEntity, 10).get should be === Inventory(10, Product(7), 5)
		}
	}

	test("update id") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = mapperDao.update(InventoryEntity, inserted, Inventory(8, inserted.product, 5))
		updated should be === Inventory(8, Product(1), 5)
		mapperDao.select(InventoryEntity, 8).get should be === updated
		mapperDao.select(InventoryEntity, 10) should be(None)
	}

	test("update from null to new value ") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = mapperDao.update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		val reUdated = mapperDao.update(InventoryEntity, updated, Inventory(10, Product(2), 8))
		reUdated.product should be === Product(2)
		mapperDao.select(InventoryEntity, 10).get should be === reUdated
		mapperDao.select(ProductEntity, 1).get should be === Product(1)
		mapperDao.select(ProductEntity, 2).get should be === Product(2)
	}

	test("update") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = mapperDao.update(InventoryEntity, inserted, Inventory(10, Product(2), 7))
		mapperDao.select(InventoryEntity, 10).get should be === updated
		mapperDao.select(ProductEntity, 1).get should be === Product(1)
		mapperDao.select(ProductEntity, 2).get should be === Product(2)
	}

	test("insert") {
		createTables
		val inventory = Inventory(10, Product(1), 5)
		val inserted = mapperDao.insert(InventoryEntity, inventory)
		inserted should be === inventory
	}

	test("select") {
		createTables
		val inserted = mapperDao.insert(InventoryEntity, Inventory(10, Product(1), 5))
		mapperDao.select(InventoryEntity, 10).get should be === inserted
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Inventory(id: Int, product: Product, stock: Int)

	case class Product(id: Int)

	object InventoryEntity extends Entity[Int, SurrogateIntId, Inventory]
	{
		val id = key("id") to (_.id)
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(id, product, stock) with Stored
	}

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)

		def constructor(implicit m) = new Product(id) with Stored
	}

}