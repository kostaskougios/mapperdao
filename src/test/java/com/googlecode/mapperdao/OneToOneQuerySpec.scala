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
class OneToOneQuerySpec extends SpecificationWithJUnit {
	import OneToOneQuerySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))
	import mapperDao._
	import queryDao._
	import TestQueries._

	"query with limits (offset only)" in {
		createTables
		val products = for (i <- 0 to 10) yield insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		query(QueryConfig(offset = Some(7)), q0Limits).toSet must_== Set(products(7), products(8), products(9), products(10))
	}

	"query with limits (limit only)" in {
		createTables
		val products = for (i <- 0 to 10) yield insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		query(QueryConfig(limit = Some(2)), q0Limits).toSet must_== Set(products(0), products(1))
	}

	"query with limits" in {
		createTables
		val products = for (i <- 0 to 10) yield insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		query(QueryConfig(offset = Some(3), limit = Some(4)), q0Limits).toSet must_== Set(products(3), products(4), products(5), products(6))
	}

	"query with skip" in {
		createTables
		val p0 = insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = insert(ProductEntity, Product(3, Inventory(7, 13)))

		query(QueryConfig(skip = Set(ProductEntity.inventory)), q0WithSkip).toSet must_== Set(Product(2, null), Product(3, null))
	}

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
		def q0Limits = select from p
		def q0WithSkip = select from p join (p, p.inventory, i) where i.stock > 5
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
		val stock = column("stock") to (_.stock)
		val sold = column("sold") to (_.sold)

		def constructor(implicit m) = new Inventory(stock, sold) with Persisted
	}
	val InventoryEntity = new InventoryEntityBase

	class ProductEntityBase extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Persisted
	}
	val ProductEntity = new ProductEntityBase
}