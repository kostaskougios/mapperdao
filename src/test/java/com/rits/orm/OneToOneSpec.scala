package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 30 Aug 2011
 */
class OneToOneSpec extends SpecificationWithJUnit {
	import OneToOneSpec._
	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._
	"insert mutable" in {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = insert(ProductEntity, product)
		inserted must_== product
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

object OneToOneSpec {
	case class Inventory(var product: Product, val stock: Int) {
		override def toString = "Inventory(%d)".format(stock)
	}
	case class Product(val id: Int, inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val product = oneToOne(classOf[Product], "product_id", _.product)
		val stock = int("stock", _.stock)

		val constructor = (m: ValuesMap) => new Inventory(m(product), m(stock)) with Persisted {
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