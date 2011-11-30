package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneWithoutReverseSpec extends SpecificationWithJUnit {
	import OneToOneWithoutReverseSpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

	if (Setup.database != "derby") {
		"update id of related" in {
			createTables
			val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
			val updatedProduct = update(ProductEntity, inserted.product, Product(7))
			updatedProduct must_== Product(7)
			select(InventoryEntity, 10).get must_== Inventory(10, Product(7), 5)
		}
	}

	"update id" in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(8, inserted.product, 5))
		updated must_== Inventory(8, Product(1), 5)
		select(InventoryEntity, 8).get must_== updated
		select(InventoryEntity, 10) must beNone
	}

	"update from null to existing value " in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product must beNull
		val reUdated = update(InventoryEntity, updated, Inventory(10, select(ProductEntity, 1).get, 8))
		reUdated.product must_== Product(1)
		select(InventoryEntity, 10).get must_== reUdated
	}

	"update from null to new value " in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product must beNull
		val reUdated = update(InventoryEntity, updated, Inventory(10, Product(2), 8))
		reUdated.product must_== Product(2)
		select(InventoryEntity, 10).get must_== reUdated
		select(ProductEntity, 1).get must_== Product(1)
		select(ProductEntity, 2).get must_== Product(2)
	}

	"update to null" in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, null, 7))
		updated.product must beNull
		select(InventoryEntity, 10).get must_== updated
		select(ProductEntity, 1).get must_== Product(1)
	}

	"update" in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		val updated = update(InventoryEntity, inserted, Inventory(10, Product(2), 7))
		select(InventoryEntity, 10).get must_== updated
		select(ProductEntity, 1).get must_== Product(1)
		select(ProductEntity, 2).get must_== Product(2)
	}

	"insert" in {
		createTables
		val inventory = Inventory(10, Product(1), 5)
		val inserted = insert(InventoryEntity, inventory)
		inserted must_== inventory
	}

	"select" in {
		createTables
		val inserted = insert(InventoryEntity, Inventory(10, Product(1), 5))
		select(InventoryEntity, 10).get must_== inserted
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