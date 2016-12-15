package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         31 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneImmutableOneWaySuite extends FunSuite
{

	import OneToOneImmutableOneWaySuite._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, InventoryEntity))

	if (Setup.database != "derby") {
		test("update id") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(1, Inventory(10)))
			val updated = mapperDao.update(ProductEntity, inserted, Product(2, Inventory(15)))
			updated should be(Product(2, Inventory(15)))
			mapperDao.select(ProductEntity, 2).get should be(updated)
			mapperDao.select(ProductEntity, 1) should be(None)
		}
	}

	test("crud for many objects") {
		createTables
		for (i <- 1 to 4) {
			val p = mapperDao.insert(ProductEntity, Product(i, Inventory(4 + i)))
			val selected = mapperDao.select(ProductEntity, i).get
			selected should be(p)
		}
	}

	test("update to null") {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		val updated = mapperDao.update(ProductEntity, inserted, Product(1, null))
		updated should be(Product(1, null))
		mapperDao.select(ProductEntity, updated.id).get should be(updated)

		val reUpdated = mapperDao.update(ProductEntity, updated, Product(1, Inventory(8)))
		reUpdated should be(Product(1, Inventory(8)))
		mapperDao.select(ProductEntity, reUpdated.id).get should be(reUpdated)

		mapperDao.delete(ProductEntity, reUpdated)
		mapperDao.select(ProductEntity, reUpdated.id) should be(None)
	}

	test("update") {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		val updated = mapperDao.update(ProductEntity, inserted, Product(1, Inventory(15)))
		updated should be(Product(1, Inventory(15)))
		mapperDao.select(ProductEntity, inserted.id).get should be(updated)
	}

	test("insert") {
		createTables
		val product = new Product(3, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be(product)
	}

	test("select") {
		createTables
		val product = new Product(8, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		mapperDao.select(ProductEntity, inserted.id).get should be(inserted)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object OneToOneImmutableOneWaySuite
{

	case class Inventory(stock: Int)

	case class Product(id: Int, inventory: Inventory)

	object InventoryEntity extends Entity[Unit, NoId, Inventory]
	{
		val stock = column("stock") to (_.stock)

		def constructor(implicit m: ValuesMap) = new Inventory(stock) with Stored
	}

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m: ValuesMap) = new Product(id, inventory) with Stored
	}

}