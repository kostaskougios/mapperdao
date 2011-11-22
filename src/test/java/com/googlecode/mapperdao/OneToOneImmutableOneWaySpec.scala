package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneImmutableOneWaySpec extends SpecificationWithJUnit {
	import OneToOneImmutableOneWaySpec._
	val (jdbc, driver, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

	if (Setup.database != "derby") {
		"update id" in {
			createTables
			val inserted = insert(ProductEntity, Product(1, Inventory(10)))
			val updated = update(ProductEntity, inserted, Product(2, Inventory(15)))
			updated must_== Product(2, Inventory(15))
			select(ProductEntity, 2).get must_== updated
			select(ProductEntity, 1) must beNone
		}
	}

	"crud for many objects" in {
		createTables
		for (i <- 1 to 4) {
			val p = insert(ProductEntity, Product(i, Inventory(4 + i)))
			val selected = select(ProductEntity, i).get
			selected must_== p
		}
		success
	}

	"update to null" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		val updated = update(ProductEntity, inserted, Product(1, null))
		updated must_== Product(1, null)
		select(ProductEntity, updated.id).get must_== updated

		val reUpdated = update(ProductEntity, updated, Product(1, Inventory(8)))
		reUpdated must_== Product(1, Inventory(8))
		select(ProductEntity, reUpdated.id).get must_== reUpdated

		delete(ProductEntity, reUpdated)
		select(ProductEntity, reUpdated.id) must beNone
	}

	"update" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		val updated = update(ProductEntity, inserted, Product(1, Inventory(15)))
		updated must_== Product(1, Inventory(15))
		select(ProductEntity, inserted.id).get must_== updated
	}

	"insert" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		inserted must_== product
	}

	"select" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = insert(ProductEntity, product)
		select(ProductEntity, inserted.id).get must_== inserted
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
}

object OneToOneImmutableOneWaySpec {
	case class Inventory(val stock: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(stock) with Persisted
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Persisted
	}
}