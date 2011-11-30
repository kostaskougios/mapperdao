package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class OneToOneImmutableOneWaySpec extends SpecificationWithJUnit {
	import OneToOneImmutableOneWaySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	if (Setup.database != "derby") {
		"update id" in {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(1, Inventory(10)))
			val updated = mapperDao.update(ProductEntity, inserted, Product(2, Inventory(15)))
			updated must_== Product(2, Inventory(15))
			mapperDao.select(ProductEntity, 2).get must_== updated
			mapperDao.select(ProductEntity, 1) must beNone
		}
	}

	"crud for many objects" in {
		createTables
		for (i <- 1 to 4) {
			val p = mapperDao.insert(ProductEntity, Product(i, Inventory(4 + i)))
			val selected = mapperDao.select(ProductEntity, i).get
			selected must_== p
		}
		success
	}

	"update to null" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		val updated = mapperDao.update(ProductEntity, inserted, Product(1, null))
		updated must_== Product(1, null)
		mapperDao.select(ProductEntity, updated.id).get must_== updated

		val reUpdated = mapperDao.update(ProductEntity, updated, Product(1, Inventory(8)))
		reUpdated must_== Product(1, Inventory(8))
		mapperDao.select(ProductEntity, reUpdated.id).get must_== reUpdated

		mapperDao.delete(ProductEntity, reUpdated)
		mapperDao.select(ProductEntity, reUpdated.id) must beNone
	}

	"update" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		val updated = mapperDao.update(ProductEntity, inserted, Product(1, Inventory(15)))
		updated must_== Product(1, Inventory(15))
		mapperDao.select(ProductEntity, inserted.id).get must_== updated
	}

	"insert" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		inserted must_== product
	}

	"select" in {
		createTables
		val product = new Product(1, Inventory(10))
		val inserted = mapperDao.insert(ProductEntity, product)
		mapperDao.select(ProductEntity, inserted.id).get must_== inserted
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
}

object OneToOneImmutableOneWaySpec {
	case class Inventory(val stock: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object InventoryEntity extends SimpleEntity[Inventory](classOf[Inventory]) {
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(stock) with Persisted
	}

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Persisted
	}
}