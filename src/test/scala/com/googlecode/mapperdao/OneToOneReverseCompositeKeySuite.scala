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
 *         30 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToOneReverseCompositeKeySuite extends FunSuite with ShouldMatchers
{

	import OneToOneReverseCompositeKeySuite._

	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(InventoryEntity, ProductEntity))
		var noiseId = 100

		// aliases
		val ie = InventoryEntity
		val pe = ProductEntity

		test("query reverse") {
			createTables()
			noise
			noise
			val inserted1 = mapperDao.insert(InventoryEntity, Inventory(100, "rc1", Product("product 1", null), 5))
			mapperDao.insert(InventoryEntity, Inventory(101, "rc1", Product("product 1a", null), 5))
			val inserted2 = mapperDao.insert(InventoryEntity, Inventory(100, "rc2", Product("product 2", null), 6))
			mapperDao.insert(InventoryEntity, Inventory(101, "rc2", Product("product 2a", null), 6))

			import Query._
			(
				select
					from pe
					join(pe, pe.inventory, ie)
					where pe.inventory === inserted2
				).toSet should be === Set(Product("product 2", Inventory(100, "rc2", Product("product 2", null), 6)))

			(
				select
					from pe
					join(pe, pe.inventory, ie)
					where pe.inventory === inserted1
				).toSet should be === Set(Product("product 1", Inventory(100, "rc1", Product("product 1", null), 5)))

			(
				select
					from pe
					join(pe, pe.inventory, ie)
					where ie.refCode === "rc1"
				).toSet should be === Set(
				Product("product 1", Inventory(100, "rc1", Product("product 1", null), 5)),
				Product("product 1a", Inventory(101, "rc1", Product("product 1a", null), 5))
			)
		}

		test("query") {
			createTables()
			noise
			noise
			mapperDao.insert(InventoryEntity, Inventory(1, "rc1", Product("product 1", null), 5))
			mapperDao.insert(InventoryEntity, Inventory(2, "rc2", Product("product 2", null), 6))

			import Query._
			(
				select
					from ie
					where ie.stock === 6
				).toSet should be === Set(Inventory(2, "rc2", Product("product 2", Inventory(2, "rc2", Product("product 2", null), 6)), 6))
		}

		test("create, select and delete") {
			createTables()
			noise
			noise
			val i = Inventory(100, "ref1", Product("product 1", null), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			inserted should be === i

			mapperDao.select(InventoryEntity, (inserted.id, inserted.refCode)).get should be === Inventory(100, "ref1", Product("product 1", Inventory(100, "ref1", Product("product 1", null), 5)), 5)

			mapperDao.delete(InventoryEntity, inserted)
			mapperDao.select(InventoryEntity, (inserted.id, inserted.refCode)) should be === None
		}

		test("delete associated with cascade") {
			createTables()
			noise
			noise
			val i = Inventory(100, "ref1", Product("rc1", null), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			val productId = Helpers.intIdOf(inserted.product)

			mapperDao.delete(DeleteConfig(propagate = true), ProductEntity, Helpers.asSurrogateIntId(inserted.product))
			mapperDao.select(InventoryEntity, (inserted.id, inserted.refCode)) should be === None
			mapperDao.select(ProductEntity, productId) should be === None
		}

		test("update") {
			createTables()
			noise
			noise
			val i = Inventory(100, "ref1", Product("rc1", null), 5)
			val inserted = mapperDao.insert(InventoryEntity, i)
			inserted should be === i

			val upd = inserted.copy(product = Product("rc2", null))
			val updated = mapperDao.update(InventoryEntity, inserted, upd)
			updated should be === upd

			mapperDao.select(InventoryEntity, (updated.id, "ref1")).get should be === Inventory(100, "ref1", Product("rc2", Inventory(100, "ref1", Product("rc2", null), 5)), 5)
		}

		def noise = {
			noiseId += 1
			mapperDao.insert(InventoryEntity, Inventory(noiseId, "noise-ref", Product("a nice & noisy product " + noiseId, null), 8))
		}

		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
			if (Setup.database == "oracle") {
				Setup.createSeq(jdbc, "ProductSeq")
			}
		}
	}
}

object OneToOneReverseCompositeKeySuite
{
	val database = Setup.database

	case class Inventory(id: Int, refCode: String, product: Product, stock: Int)

	case class Product(name: String, inventory: Inventory)

	object InventoryEntity extends Entity[(Int, String), NaturalIntAndNaturalStringIds,Inventory]
	{
		val id = key("id") to (_.id)
		val refCode = key("refCode") to (_.refCode)
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		def constructor(implicit m) = new Inventory(id, refCode, product, stock) with Stored
	}

	object ProductEntity extends Entity[Int,SurrogateIntId, Product]
	{
		val id = key("id") sequence (
			if (database == "oracle") Some("ProductSeq") else None
			) autogenerated (_.id)
		val name = column("name") to (_.name)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(name, inventory) with Stored
		{
			val id: Int = ProductEntity.id
		}
	}

}
