package com.googlecode.mapperdao
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyLazyLoadSuite extends FunSuite with ShouldMatchers {

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	if (Setup.database == "h2") {
		test("update id of main entity") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			mapperDao.select(SelectConfig(lazyLoad = LazyLoad(all = true)), ProductEntity, 2).get should be === Product(2, "blue jean", inserted.attributes)
		}
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity[Product] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) getter ("attributes") to (_.attributes)
		def constructor(implicit m) = new Product(id, name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity[Attribute] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with Persisted
	}
}