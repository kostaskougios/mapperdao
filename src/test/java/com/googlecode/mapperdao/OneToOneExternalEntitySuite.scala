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
			val selected = mapperDao.select(ProductEntity, inserted.id).get
			selected should be === inserted
		}
		test("update/select") {
			createTables
			val inserted = mapperDao.insert(ProductEntity, Product(5, Inventory(105, 205)))
			mapperDao.update(ProductEntity, inserted, Product(5, Inventory(106, 206)))
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
		def primaryKeyValues(inventory) = (inventory.id, None)
		override def selectOneToOneReverse(foreignIds) = foreignIds match {
			case (foreignId, _) => new Inventory(foreignId + 100, 200 + foreignId)
		}
	}
}