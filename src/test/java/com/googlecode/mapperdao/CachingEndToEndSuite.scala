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

	test("update, many to many, add related, flushes cached") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(ProductEntity, 5)

		val updated = mapperDao.update(ProductEntity, inserted, Product(5, "xxx", inserted.attributes + Attribute(8, "size", "large")))
		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), ProductEntity, 5) should be === Some(updated)
	}

	test("update, many to many, remove related, flushes cached") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(ProductEntity, 5)

		val updated = mapperDao.update(ProductEntity, inserted, Product(5, "xxx", inserted.attributes.filter(_.id != 2)))
		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), ProductEntity, 5) should be === Some(updated)
	}

	test("main entity selection is cached") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(ProductEntity, 5)

		// manually delete rows
		deleteAll()
		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), ProductEntity, 5) should be === Some(inserted)
	}

	test("main entity selection is cached but expired") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(ProductEntity, 5)

		// manually delete rows
		deleteAll()

		Thread.sleep(15)
		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions(2)), ProductEntity, 5) should be === None
	}

	test("secondary entity selection is cached") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		// do a dummy select, just to cache it
		mapperDao.select(AttributeEntity, 2)

		// manually delete rows
		deleteAll()
		// still cached
		mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), AttributeEntity, 2) should be === Some(Attribute(2, "colour", "blue"))
	}

	test("query with cached data positive") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		import Query._
		// just to cache the data
		queryDao.query(select from ProductEntity)
		deleteAll()
		// even if the data are deleted, the cached values will be used
		queryDao.query(QueryConfig(cacheOptions = CacheOptions.OneHour), select from ProductEntity) should be === List(inserted)
	}

	test("query with cached data negative") {
		createTables
		val product = Product(5, "blue jean", Set(Attribute(2, "colour", "blue"), Attribute(7, "size", "medium")))
		val inserted = mapperDao.insert(ProductEntity, product)

		import Query._
		// just to cache the data
		queryDao.query(select from ProductEntity)
		deleteAll()
		Thread.sleep(15)
		// even if the data are deleted, the cached values will be used
		queryDao.query(QueryConfig(cacheOptions = CacheOptions(3)), select from ProductEntity) should be === List()
	}

	def deleteAll() {
		jdbc.update("delete from Product")
		jdbc.update("delete from Product_Attribute")
		jdbc.update("delete from Attribute")
	}
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