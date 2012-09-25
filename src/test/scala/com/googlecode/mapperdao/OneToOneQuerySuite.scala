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
class OneToOneQuerySuite extends FunSuite with ShouldMatchers {
	val InventoryEntity = new InventoryEntityBase
	val ProductEntity = new ProductEntityBase

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	test("query with limits (offset only)") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(offset = Some(7)), q0Limits).toSet should be === Set(products(7), products(8), products(9), products(10))
	}

	test("query with limits (limit only)") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(limit = Some(2)), q0Limits).toSet should be === Set(products(0), products(1))
	}

	test("query with limits") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(offset = Some(3), limit = Some(4)), q0Limits).toSet should be === Set(products(3), products(4), products(5), products(6))
	}

	test("query with skip") {
		createTables
		val p0 = mapperDao.insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = mapperDao.insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = mapperDao.insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = mapperDao.insert(ProductEntity, Product(3, Inventory(7, 13)))

		queryDao.query(QueryConfig(skip = Set(ProductEntity.inventory)), q0WithSkip).toSet should be === Set(Product(2, null), Product(3, null))
	}

	test("query by inventory.stock") {
		createTables
		val p0 = mapperDao.insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = mapperDao.insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = mapperDao.insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = mapperDao.insert(ProductEntity, Product(3, Inventory(7, 13)))

		queryDao.query(q0).toSet should be === Set(p2, p3)
	}

	test("query with and") {
		createTables
		val p0 = mapperDao.insert(ProductEntity, Product(0, Inventory(4, 10)))
		val p1 = mapperDao.insert(ProductEntity, Product(1, Inventory(5, 11)))
		val p2 = mapperDao.insert(ProductEntity, Product(2, Inventory(6, 12)))
		val p3 = mapperDao.insert(ProductEntity, Product(3, Inventory(7, 13)))

		queryDao.query(q1).toSet should be === Set(p2)
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

	case class Inventory(val stock: Int, val sold: Int)
	case class Product(val id: Int, val inventory: Inventory)

	class InventoryEntityBase extends Entity[NoId, Inventory] {
		val stock = column("stock") to (_.stock)
		val sold = column("sold") to (_.sold)

		def constructor(implicit m) = new Inventory(stock, sold) with NoId
	}

	class ProductEntityBase extends Entity[IntId, Product] {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with IntId
	}
}