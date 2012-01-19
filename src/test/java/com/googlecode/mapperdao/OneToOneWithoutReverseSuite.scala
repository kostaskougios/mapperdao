package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneWithoutReverseSuite extends FunSuite with ShouldMatchers {
	import OneToOneWithoutReverseSpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

	if (Setup.database != "derby") {
		test("update id of related") {
			createTables
			val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
			val updatedProduct = update(ProductEntity, inserted.product, Product(7))
			updatedProduct should be === Product(7)
			select(InventoryEntity, 10).get should be === Inventory(10, Product(7), 5)
		}
	}

	test("update id") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(8, inserted.product, 5))
		updated should be === Inventory(8, Product(1), 5)
		select(InventoryEntity, 8).get should be === updated
		select(InventoryEntity, 10) should be(None)
	}

	test("update from null to existing value ") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		val reUdated = update(InventoryEntity, updated, Inventory(10, select(ProductEntity, 1).get, 8))
		reUdated.product should be === Product(1)
		select(InventoryEntity, 10).get should be === reUdated
	}

	test("update from null to new value ") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		val reUdated = update(InventoryEntity, updated, Inventory(10, Product(2), 8))
		reUdated.product should be === Product(2)
		select(InventoryEntity, 10).get should be === reUdated
		select(ProductEntity, 1).get should be === Product(1)
		select(ProductEntity, 2).get should be === Product(2)
	}

	test("update to null") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product should be(null)
		select(InventoryEntity, 10).get should be === updated
		select(ProductEntity, 1).get should be === Product(1)
	}

	test("update") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, Product(2), 7))
		select(InventoryEntity, 10).get should be === updated
		select(ProductEntity, 1).get should be === Product(1)
		select(ProductEntity, 2).get should be === Product(2)
	}

	test("insert") {
		createTables
		val inventory = Inventory(10, Product(1), 5)
		val inserted = insert(InventoryEntity, inventory)
		inserted should be === inventory
	}

	test("select") {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		select(InventoryEntity, 10).get should be === inserted
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
}

object OneToOneWithoutReverseSpec {
	case class Inventory(val id: Int, val product: Product, val stock: Int)
	case class Product(val id: Int)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val id = key("id") to (_.id)
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(id, product, stock) with Persisted
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)

		def constructor(implicit m) = new Product(id) with Persisted
	}

}