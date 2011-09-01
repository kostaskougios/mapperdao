package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
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

	"insert" in {
		createTables
		val inventory = Inventory(Product(1), 5)
		val inserted = insert(InventoryEntity, inventory)
		inserted must_== inventory
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

object OneToOneWithoutReverseSpec {
	case class Inventory(val product: Product, val stock: Int)
	case class Product(val id: Int)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val product = oneToOne(classOf[Product], "product_id", _.product)
		val stock = int("stock", _.stock)

		val constructor = (m: ValuesMap) => new Inventory(m(product), m(stock)) with Persisted {
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