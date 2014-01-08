package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}
import com.googlecode.mapperdao.Query._

/**
 * @author kostantinos.kougios
 *
 *         1 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneQuerySuite extends FunSuite with Matchers
{
	val p = ProductEntity
	val i = InventoryEntity

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, InventoryEntity))

	test("query with alias") {
		createTables
		val List(p0, _, p2, _) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(0, Inventory(4, 10)),
				Product(1, Inventory(5, 11)),
				Product(2, Inventory(6, 10)),
				Product(3, Inventory(7, 13))
			)
		)
		queryDao.query(
			select from p
				join (p as 'p1) on ('p1, p.id) <> p.id
				join(p, p.inventory, i)
				join(p as 'p1, p.inventory, i as 'i1) on ('i1, i.sold) === i.sold
		).toSet should be === Set(p0, p2)
	}

	test("query with limits (offset only)") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(offset = Some(7)), select from p).toSet should be === Set(products(7), products(8), products(9), products(10))
	}

	test("query with limits (limit only)") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(limit = Some(2)), select from p).toSet should be === Set(products(0), products(1))
	}

	test("query with limits") {
		createTables
		val products = for (i <- 0 to 10) yield mapperDao.insert(ProductEntity, Product(i, Inventory(10 + i, 15 + i)))
		queryDao.query(QueryConfig(offset = Some(3), limit = Some(4)), select from p).toSet should be === Set(products(3), products(4), products(5), products(6))
	}

	test("query with skip") {
		createTables
		mapperDao.insertBatch(ProductEntity,
			List(
				Product(0, Inventory(4, 10)),
				Product(1, Inventory(5, 11)),
				Product(2, Inventory(6, 12)),
				Product(3, Inventory(7, 13))
			)
		)

		queryDao.query(QueryConfig(skip = Set(ProductEntity.inventory)),
			select from p join(p, p.inventory, i) where i.stock > 5
		).toSet should be === Set(Product(2, null), Product(3, null))
	}

	test("query by inventory.stock") {
		createTables
		val List(_, _, p2, p3) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(0, Inventory(4, 10)),
				Product(1, Inventory(5, 11)),
				Product(2, Inventory(6, 12)),
				Product(3, Inventory(7, 13))
			)
		)
		queryDao.query(select from p join(p, p.inventory, i) where i.stock > 5).toSet should be === Set(p2, p3)
	}

	test("query with and") {
		createTables
		val List(_, _, p2, _) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(0, Inventory(4, 10)),
				Product(1, Inventory(5, 11)),
				Product(2, Inventory(6, 12)),
				Product(3, Inventory(7, 13))
			)
		)

		queryDao.query(
			select from p
				join(p, p.inventory, i)
				where
				i.stock > 5
				and i.sold < 13
		).toSet should be === Set(p2)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		jdbc.update( """
				create table Product (
					id int not null,
					primary key (id)
				)
					 """)
		jdbc.update( """
				create table Inventory (
					product_id int not null,
					stock int not null,
					sold int not null,
					primary key (product_id),
					foreign key (product_id) references Product(id) on delete cascade
				)
					 """)
	}

	case class Inventory(stock: Int, sold: Int)

	case class Product(id: Int, inventory: Inventory)

	object InventoryEntity extends Entity[Unit, NoId, Inventory]
	{
		val stock = column("stock") to (_.stock)
		val sold = column("sold") to (_.sold)

		def constructor(implicit m: ValuesMap) = new Inventory(stock, sold) with Stored
	}

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m: ValuesMap) = new Product(id, inventory) with Stored
	}

}