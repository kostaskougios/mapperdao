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
			val product = Product(5, Inventory(10, 20))
			val inserted = mapperDao.insert(ProductEntity, product)
			inserted should be === product
			val selected = mapperDao.select(ProductEntity, inserted.id)
			selected should be === inserted
		}
	}
	def createTables =
		{
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
	}
}