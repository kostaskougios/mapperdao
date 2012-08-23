package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 */
@RunWith(classOf[JUnitRunner])
class OneToOneDeclarePrimaryKeySuite extends FunSuite with ShouldMatchers {
	implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, InventoryEntity))

	val p = ProductEntity
	val i = InventoryEntity
	test("query on product") {
		createTables

		import Query._
		val inventories = for (i <- 0 to 10) yield mapperDao.insert(InventoryEntity, Inventory(Product(1 + i), 5 + i))
		val p2 = Helpers.asIntId(inventories(2).product)
		(
			select
			from i
			join (i, i.product, p)
			where i.product === p2
		).toSet should be === Set(inventories(2))
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	case class Inventory(val product: Product, val stock: Int)
	case class Product(val id: Int)

	object InventoryEntity extends SimpleEntity[Inventory] {
		val product = onetoone(ProductEntity) to (_.product)
		val stock = column("stock") to (_.stock)

		declarePrimaryKey(product)

		def constructor(implicit m) = new Inventory(product, stock) with Persisted
	}

	object ProductEntity extends SimpleEntity[Product] {
		val id = key("id") to (_.id)

		def constructor(implicit m) = new Product(id) with Persisted
	}
}