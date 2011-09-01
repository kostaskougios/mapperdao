package com.rits.orm

import org.specs2.mutable.SpecificationWithJUnit
import com.rits.jdbc.Jdbc
import com.rits.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 1 Sep 2011
 */
class OneToOneQuerySpec extends SpecificationWithJUnit {
	import OneToOneQuerySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(ProductEntity, InventoryEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

	"query by inventory.stock" in {
		createTables
		val p0 = insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = insert(ProductEntity, Product(3, Inventory(7, 13)))

		query(q0).toSet must_== Set(p2, p3)
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
					sold int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade
				)
			""")
		}
}

object OneToOneQuerySpec {

	object TestQueries {
		val p = ProductEntity
		val i = InventoryEntity
		import Query._
		def q0 = select from p join (p, p.inventory, i) where i.stock > 5
	}

	case class Inventory(val stock: Int, val sold: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = int("stock", _.stock)
		val sold = int("sold", _.sold)

		val constructor = (m: ValuesMap) => new Inventory(m(stock), m(sold)) with Persisted {
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