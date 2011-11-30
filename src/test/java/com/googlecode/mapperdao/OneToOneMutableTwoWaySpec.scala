package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 30 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneMutableTwoWaySpec extends SpecificationWithJUnit {
	import OneToOneMutableTwoWaySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	if (Setup.database != "derby") {
		"update id of primary entity" in {
			createTables
			val product = Product(1, Inventory(null, 5))
			product.inventory.product = product
			val inserted = mapperDao.insert(ProductEntity, product)
			inserted.id = 2
			val updated = mapperDao.update(ProductEntity, inserted)
			updated must_== inserted
			val recreatedProduct = Product(2, Inventory(null, 5))
			recreatedProduct.inventory.product = recreatedProduct
			mapperDao.select(ProductEntity, 2).get must_== recreatedProduct
			mapperDao.select(ProductEntity, 1) must beNone
		}
	}

	"CRUD mutable" in {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory.stock = 8
		val updated = mapperDao.update(ProductEntity, inserted)
		updated must_== inserted
		val selected = mapperDao.select(ProductEntity, updated.id).get
		selected must_== updated

		mapperDao.delete(ProductEntity, selected)
		mapperDao.select(ProductEntity, selected.id) must beNone
	}

	"insert & select mutable" in {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product
		val selected = mapperDao.select(ProductEntity, inserted.id).get
		selected must_== inserted
	}

	"from null to value" in {
		createTables
		val product = Product(1, null)
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory = Inventory(inserted, 5)
		val updated = mapperDao.update(ProductEntity, inserted)
		updated.inventory must_== inserted.inventory
		mapperDao.select(ProductEntity, inserted.id).get must_== updated

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) must beNone
	}

	"update to null" in {
		createTables
		val product = Product(1, Inventory(null, 5))
		product.inventory.product = product
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted.inventory = null
		val updated = mapperDao.update(ProductEntity, inserted)
		updated.inventory must_== null
		mapperDao.select(ProductEntity, inserted.id).get must_== updated
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
}

object OneToOneMutableTwoWaySpec {
	case class Inventory(var product: Product, var stock: Int) {
		override def hashCode = stock
		override def equals(v: Any) = v match {
			case i: Inventory => i.stock == stock && ((i.product == null && product == null) || (i.product != null && product != null && i.product.id == product.id))
			case _ => false
		}
		override def toString = "Inventory(%d, productId:%d)".format(stock, if (product == null) null else product.id)
	}
	case class Product(var id: Int, var inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(product, stock) with Persisted
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Persisted
	}
}