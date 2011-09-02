package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
class OneToOneWithoutReverseSpec extends SpecificationWithJUnit {
	import OneToOneWithoutReverseSpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

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
			jdbc.update("drop table if exists Product cascade")
			jdbc.update("drop table if exists Inventory cascade")

			jdbc.update("""
				create table Product (
					id int not null,
					primary key (id)
				)
			""")
			jdbc.update("""
				create table Inventory (
					id int not null,
					product_id int,
					stock int not null,
					primary key (id),
					foreign key (product_id) references Product(id) on delete cascade
				)
			""")
		}
}

object OneToOneWithoutReverseSpec {
	case class Inventory(val id: Int, val product: Product, val stock: Int)
	case class Product(val id: Int)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val id = pk("id", _.id)
		val product = oneToOne(classOf[Product], "product_id", _.product)
		val stock = int("stock", _.stock)

		val constructor = (m: ValuesMap) => new Inventory(m(id), m(product), m(stock)) with Persisted {
			val valuesMap = m
		}
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = pk("id", _.id)

		val constructor = (m: ValuesMap) => new Product(m(id)) with Persisted {
			val valuesMap = m
		}
	}

}