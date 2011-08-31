package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToOneImmutableOneWaySpec extends SpecificationWithJUnit {
	import OneToOneImmutableOneWaySpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._

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
					product_id int not null,
					stock int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade
				)
			""")
		}
}

object OneToOneImmutableOneWaySpec {
	case class Inventory(val stock: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = int("stock", _.stock)

		val constructor = (m: ValuesMap) => new Inventory(m(stock)) with Persisted {
			val valuesMap = m
		}
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = pk("id", _.id)
		val inventory = oneToOneReverse(classOf[Inventory], "product_id", _.inventory)

		val constructor = (m: ValuesMap) => new Product(m(id), m(inventory)) with Persisted {
			val valuesMap = m
		}
	}
}