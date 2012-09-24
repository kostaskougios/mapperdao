package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 * 29 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToOneCompositeKeySuite extends FunSuite with ShouldMatchers {

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(InventoryEntity, ProductEntity))

		// aliases
		val ie = InventoryEntity
		val pe = ProductEntity

		test("query") {
			createTables()
			noise
			noise
			val inserted1 = mapperDao.insert(InventoryEntity, Inventory(Product("rc1"), 5))
			val inserted2 = mapperDao.insert(InventoryEntity, Inventory(Product("rc2"), 6))
			val rc1 = Helpers.asIntId(inserted1.product)
			import Query._
			(
				select
				from ie
				where ie.stock === 6
			).toSet should be === Set(inserted2)

			(
				select
				from ie
				join (ie, ie.product, pe)
				where pe.refCode === "rc2"
			).toSet should be === Set(inserted2)

			(
				select
				from ie
				join (ie, ie.product, pe)
				where pe.refCode === "rc2" or pe.refCode === "rc1"
			).toSet should be === Set(inserted1, inserted2)

			(
				select
				from ie
				join (ie, ie.product, pe)
				where ie.product === rc1
			).toSet should be === Set(inserted1)

		}

		test("create, select and delete") {
			createTables()
			noise
			noise
			val i = Inventory(Product("rc1"), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			inserted should be === i

			mapperDao.select(InventoryEntity, inserted.id).get should be === inserted

			mapperDao.delete(InventoryEntity, inserted.id)
			mapperDao.select(InventoryEntity, inserted.id) should be === None
		}

		test("delete associated with cascade") {
			createTables()
			noise
			noise
			val i = Inventory(Product("rc1"), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			val productId = Helpers.intIdOf(inserted.product)

			mapperDao.delete(DeleteConfig(propagate = true), ProductEntity, Helpers.asIntId(inserted.product))
			mapperDao.select(InventoryEntity, inserted.id) should be === None
			mapperDao.select(ProductEntity, productId, "rc1") should be === None
		}

		test("update") {
			createTables()
			noise
			noise
			val i = Inventory(Product("rc1"), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			inserted should be === i

			val upd = inserted.copy(product = Product("rc2"))
			val updated = mapperDao.update(InventoryEntity, inserted, upd)
			updated should be === upd

			mapperDao.select(InventoryEntity, updated.id).get should be === updated
		}

		def noise = mapperDao.insert(InventoryEntity, Inventory(Product("rcX"), 8))

		def createTables() =
			{
				Setup.dropAllTables(jdbc)
				Setup.queries(this, jdbc).update("ddl")
				if (Setup.database == "oracle") {
					Setup.createSeq(jdbc, "InventorySeq")
					Setup.createSeq(jdbc, "ProductSeq")
				}
			}
	}
	case class Inventory(product: Product, stock: Int)
	case class Product(refCode: String)

	object InventoryEntity extends Entity[IntId, Inventory] {
		val id = key("id") sequence (
			if (database == "oracle") Some("InventorySeq") else None
		) autogenerated (_.id)
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(product, stock) with Persisted with IntId {
			val id: Int = InventoryEntity.id
		}
	}

	object ProductEntity extends Entity[IntId, Product] {
		val id = key("id") sequence (
			if (database == "oracle") Some("ProductSeq") else None
		) autogenerated (_.id)
		val refCode = key("refCode") to (_.refCode)

		def constructor(implicit m) = new Product(refCode) with Persisted with IntId {
			val id: Int = ProductEntity.id
		}
	}
}