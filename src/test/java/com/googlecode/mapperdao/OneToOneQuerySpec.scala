package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.jdbc.Setup

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

	"query with and" in {
		createTables
		val p0 = insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = insert(ProductEntity, Product(3, Inventory(7, 13)))

		query(q1).toSet must_== Set(p2)
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
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
		def q1 = (
			select from p
			join (p, p.inventory, i)
			where
			i.stock > 5
			and i.sold < 13
		)
		//def q2(inventory: Inventory) = select from p where p.inventory === inventory
	}

	case class Inventory(val stock: Int, val sold: Int)
	case class Product(val id: Int, val inventory: Inventory)

	class InventoryEntityBase extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = int("stock", _.stock)
		val sold = int("sold", _.sold)

		def constructor(implicit m: ValuesMap) = new Inventory(stock, sold) with Persisted {
			val valuesMap = m
		}
	}
	val InventoryEntity = new InventoryEntityBase

	class ProductEntityBase extends SimpleEntity[Product](classOf[Product]) {
		val id = intPK("id", _.id)
		val inventory = oneToOneReverse(classOf[Inventory], _.inventory)

		def constructor(implicit m: ValuesMap) = new Product(id, inventory) with Persisted {
			val valuesMap = m
		}
	}
	val ProductEntity = new ProductEntityBase
}