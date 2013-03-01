package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         24 Jan 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToOneReverseExternalEntitySuite extends FunSuite with ShouldMatchers
{
	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	if (Setup.database == "h2") {
		test("persist/select") {
			createTables
			val product = Product(5, Inventory(105, 205))
			val inserted = mapperDao.insert(ProductEntity, product)
			inserted should be === product
			InventoryEntity.onInsertCalled should be === 1
			val selected = mapperDao.select(ProductEntity, inserted.id).get
			selected should be === inserted
		}
		test("update/select") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			val updated = mapperDao.update(ProductEntity, inserted, Product(5, Inventory(106, 206)))
			InventoryEntity.onUpdateCalled should be === 1
			mapperDao.select(ProductEntity, inserted.id).get should be === updated
		}
		test("delete without propagate") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			mapperDao.delete(ProductEntity, inserted)
			InventoryEntity.onDeleteCalled should be === 0
			mapperDao.select(ProductEntity, inserted.id) should be(None)
		}

		test("delete with propagate") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			mapperDao.delete(DeleteConfig(propagate = true), ProductEntity, inserted)
			InventoryEntity.onDeleteCalled should be === 1
			mapperDao.select(ProductEntity, inserted.id) should be(None)
		}

		test("query") {
			createTables
			val inserted1 = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			mapperDao.insert(ProductEntity, Product(6, Inventory(106, 206)))
			import Query._
			val pe = ProductEntity
			queryDao.query(select from pe where pe.id === 5) should be === List(inserted1)
		}
	}

	def createTables {
		InventoryEntity.onInsertCalled = 0
		InventoryEntity.onUpdateCalled = 0
		InventoryEntity.onDeleteCalled = 0
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Inventory(val id: Int, val stock: Int)

	case class Product(val id: Int, val inventory: Inventory)

	object ProductEntity extends Entity[Int, Product]
	{
		type Stored = SurrogateIntId
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Stored
	}

	object InventoryEntity extends ExternalEntity[Int, Inventory]
	{

		var inventory = Map[Int, Inventory]()
		var onInsertCalled = 0
		onInsertOneToOneReverse(ProductEntity.inventory) {
			case InsertExternalOneToOneReverse(updateConfig, entity, foreign) =>
				onInsertCalled += 1
				inventory = inventory + (entity.id -> foreign)
		}

		onSelectOneToOneReverse(ProductEntity.inventory) {
			_.foreignIds match {
				case (foreignId: Int) :: Nil => inventory(foreignId)
				case _ => throw new RuntimeException
			}
		}

		var onUpdateCalled = 0
		onUpdateOneToOneReverse(ProductEntity.inventory) {
			u =>
				onUpdateCalled += 1
				inventory = inventory + (u.entity.id -> u.foreign)
		}

		var onDeleteCalled = 0
		onDeleteOneToOneReverse(ProductEntity.inventory) {
			d =>
				inventory -= d.entity.id
				onDeleteCalled += 1
		}
	}

}