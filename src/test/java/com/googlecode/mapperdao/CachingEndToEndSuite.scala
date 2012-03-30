package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import net.sf.ehcache.CacheManager
import org.scalatest.BeforeAndAfterAll
import ehcache.CacheUsingEHCache

/**
 * @author kostantinos.kougios
 *
 * 29 Mar 2012
 */
@RunWith(classOf[JUnitRunner])
class CachingEndToEndSuite extends FunSuite with ShouldMatchers {
	val cacheManager = CacheManager.create
	val ehCache = cacheManager.getCache("CachingEndToEndSuite")
	val mapperDaoCache = new CacheUsingEHCache(ehCache)

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity), cache = Some(mapperDaoCache))

	test("main entity selection is cached") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(ProductEntity, 5)

		// manually delete rows
		jdbc.update("delete from Product")

		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), ProductEntity, 5) should be === Some(inserted)
	}

	//override def afterAll = cacheManager.shutdown()

	def createTables =
		{
			ehCache.flush()
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity[Product] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)
		def constructor(implicit m) = new Product(id, name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity[Attribute] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with Persisted
	}

}