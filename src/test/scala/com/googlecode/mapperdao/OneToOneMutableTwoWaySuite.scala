package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

/**
 * @author kostantinos.kougios
 *
 *         30 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneMutableTwoWaySuite extends FunSuite with Matchers
{

	import OneToOneMutableTwoWaySuite._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, InventoryEntity))

	if (Setup.database != "derby") {
		test("update id of primary entity") {
			createTables
			val product = Product(1, Inventory(null, 5))
			product.inventory.product = product
			val inserted = mapperDao.insert(ProductEntity, product)
			inserted.id = 2
			val updated = mapperDao.update(ProductEntity, inserted)
			updated should be === inserted
			val recreatedProduct = Product(2, Inventory(null, 5))
			recreatedProduct.inventory.product = recreatedProduct
			mapperDao.select(ProductEntity, 2).get should be === recreatedProduct
			mapperDao.select(ProductEntity, 1) should be(None)
		}
	}

	test("CRUD mutable") {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory.stock = 8
		val updated = mapperDao.update(ProductEntity, inserted)
		updated should be === inserted
		val selected = mapperDao.select(ProductEntity, updated.id).get
		selected should be === updated

		mapperDao.delete(ProductEntity, selected)
		mapperDao.select(ProductEntity, selected.id) should be(None)
	}

	test("insert & select mutable") {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted should be === product
		val selected = mapperDao.select(ProductEntity, inserted.id).get
		selected should be === inserted
	}

	test("from null to value") {
		createTables
		val product = Product(1, null)
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory = Inventory(inserted, 5)
		val updated = mapperDao.update(ProductEntity, inserted)
		updated.inventory should be === inserted.inventory
		mapperDao.select(ProductEntity, inserted.id).get should be === updated

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) should be(None)
	}

	test("update to null") {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory = null
		val updated = mapperDao.update(ProductEntity, inserted)
		updated.inventory should be === null
		mapperDao.select(ProductEntity, inserted.id).get should be === updated
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}
}

object OneToOneMutableTwoWaySuite
{

	case class Inventory(var product: Product, var stock: Int)
	{
		override def hashCode = stock

		override def equals(v: Any) = v match {
			case i: Inventory => i.stock == stock && ((i.product == null && product == null) || (i.product != null && product != null && i.product.id == product.id))
			case _ => false
		}

		override def toString = "Inventory(%d, productId:%d)".format(stock, if (product == null) null else product.id)
	}

	case class Product(var id: Int, var inventory: Inventory)

	object InventoryEntity extends Entity[Unit, NoId, Inventory]
	{
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(product, stock) with Stored
	}

	object ProductEntity extends Entity[Int, NaturalIntId, Product]
	{
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Stored
	}

}