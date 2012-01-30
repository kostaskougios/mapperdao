package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 24 Jan 2012
 */
@RunWith(classOf[JUnitRunner])
class OneToOneExternalEntitySuite extends FunSuite with ShouldMatchers {
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
			mapperDao.update(ProductEntity, inserted, Product(5, Inventory(106, 206)))
			InventoryEntity.onUpdateCalled should be === 1
			// since no update of Inventory occurs, the InventoryEntity will just
			// return Inventory(105, 205)
			mapperDao.select(ProductEntity, inserted.id).get should be === inserted
		}
		test("delete") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			mapperDao.delete(ProductEntity, inserted)
			mapperDao.select(ProductEntity, inserted.id) should be(None)
		}

		test("query") {
			createTables
			val inserted1 = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			val inserted2 = mapperDao.insert(ProductEntity, Product(6, Inventory(106, 206)))
			import Query._
			val pe = ProductEntity
			queryDao.query(select from pe where pe.id === 5) should be === List(inserted1)
		}
	}
	def createTables {
		InventoryEntity.onInsertCalled = 0
		InventoryEntity.onUpdateCalled = 0
		Setup.dropAllTables(jdbc)
		Setup.queries(this, jdbc).update("ddl")
	}

	case class Inventory(val id: Int, val stock: Int)
	case class Product(val id: Int, val inventory: Inventory)

	object ProductEntity extends SimpleEntity[Product](classOf[Product]) {
		val id = key("id") to (_.id)
		val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

		def constructor(implicit m) = new Product(id, inventory) with Persisted
	}
	object InventoryEntity extends ExternalEntity[Int, Unit, Inventory](classOf[Inventory]) {
		override def primaryKeyValues(inventory) = throw new IllegalStateException //(inventory.id, None)

		var onInsertCalled = 0
		onInsertOneToOne(ProductEntity.inventory) { i =>
			onInsertCalled += 1
		}

		onSelectOneToOne(ProductEntity.inventory) {
			_.foreignIds match {
				case (foreignId: Int) :: Nil => new Inventory(foreignId + 100, 200 + foreignId)
				case _ => throw new RuntimeException
			}
		}

		var onUpdateCalled = 0
		onUpdateOneToOne(ProductEntity.inventory) { u =>
			onUpdateCalled += 1
		}
	}
}