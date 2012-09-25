package com.googlecode.mapperdao
import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.classgenerator.ReflectionManager

/**
 * @author kostantinos.kougios
 *
 * April 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyLazyLoadSuite extends FunSuite with ShouldMatchers {

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))
	val reflectionManager = new ReflectionManager

	if (Setup.database == "h2") {

		test("unlink doesn't load the lazy loaded") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val p1 = mapperDao.insert(PropertyEntity, Property(100, "p1", "v1"))
			val p2 = mapperDao.insert(PropertyEntity, Property(101, "p2", "v2"))

			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2), Set(p1, p2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			val x = selected.id
			mapperDao.unlink(ProductEntity, selected)
			// use reflection to detect that the field wasn't set
			val r1: Set[Attribute] = reflectionManager.get("attributes", selected)
			r1 should be(Set())
			val r2: Set[Attribute] = reflectionManager.get("properties", selected)
			r2 should be(Set())

			selected should be === Product(2, "blue jean", inserted.attributes, inserted.properties)
			selected.attributes should be === inserted.attributes
			selected.properties should be === inserted.properties
		}

		test("lazy load 1 of 2 related entities") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val p1 = mapperDao.insert(PropertyEntity, Property(100, "p1", "v1"))
			val p2 = mapperDao.insert(PropertyEntity, Property(101, "p2", "v2"))

			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2), Set(p1, p2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.some(Set(ProductEntity.properties))), ProductEntity, 2).get
			// use reflection to detect that the field wasn't set
			val r1: Set[Attribute] = reflectionManager.get("attributes", selected)
			r1 should be(Set(a1, a2))
			val r2: Set[Attribute] = reflectionManager.get("properties", selected)
			r2 should be(Set())
			verifyPropertiesNotLoadded(selected)

			selected should be === Product(2, "blue jean", inserted.attributes, inserted.properties)
			selected.attributes should be === inserted.attributes
			selected.properties should be === inserted.properties
		}

		test("free lazy loaded") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val p1 = mapperDao.insert(PropertyEntity, Property(100, "p1", "v1"))
			val p2 = mapperDao.insert(PropertyEntity, Property(101, "p2", "v2"))

			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2), Set(p1, p2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			mapperDao.unlinkLazyLoadMemoryData(ProductEntity, selected)
			// use reflection to detect that the field wasn't set
			val r1: Set[Attribute] = reflectionManager.get("attributes", selected)
			r1 should be(Set())
			val r2: Set[Attribute] = reflectionManager.get("properties", selected)
			r2 should be(Set())

			selected should be === Product(2, "blue jean", Set(), Set())
		}

		test("lazy load 1 of 2 related entities and update it") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val p1 = mapperDao.insert(PropertyEntity, Property(100, "p1", "v1"))
			val p2 = mapperDao.insert(PropertyEntity, Property(101, "p2", "v2"))

			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2), Set(p1, p2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.some(Set(ProductEntity.properties))), ProductEntity, 2).get
			val updated = mapperDao.update(ProductEntity, selected, Product(2, "blue jean", Set(a1), Set(p1)))
			updated should be === Product(2, "blue jean", Set(a1), Set(p1))
			val reloaded = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.some(Set(ProductEntity.properties))), ProductEntity, 2).get
			reloaded should be === updated
		}

		test("lazy load 2 related entities") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val p1 = mapperDao.insert(PropertyEntity, Property(100, "p1", "v1"))
			val p2 = mapperDao.insert(PropertyEntity, Property(101, "p2", "v2"))

			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2), Set(p1, p2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			// use reflection to detect that the field wasn't set
			val r1: Set[Attribute] = reflectionManager.get("attributes", selected)
			r1 should be(Set())
			val r2: Set[Attribute] = reflectionManager.get("properties", selected)
			r2 should be(Set())

			verifyNotLoadded(selected)

			selected should be === Product(2, "blue jean", inserted.attributes, inserted.properties)
			selected.attributes should be === inserted.attributes
			selected.properties should be === inserted.properties
		}

		test("querying, lazy load") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val i1 = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1)))
			val i2 = mapperDao.insert(ProductEntity, Product(3, "green jean", Set(a2)))

			import Query._
			val l = queryDao.query(QueryConfig(lazyLoad = LazyLoad.all), select from ProductEntity)
			val s1 = l.head
			val s2 = l.last
			verifyNotLoadded(s1)
			verifyNotLoadded(s2)

			s1 should be === i1
			s2 should be === i2
		}

		test("update immutable entity, skip lazy loaded") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			val updated = mapperDao.update(UpdateConfig(skip = Set(ProductEntity.attributes, ProductEntity.properties)), ProductEntity, selected, Product(2, "blue jean new", Set(a1)))
			verifyNotLoadded(selected)
			val reloaded = mapperDao.select(ProductEntity, 2).get
			reloaded should be === Product(2, "blue jean new", Set(a1, a2))
		}

		test("update mutable entity") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			selected.attributes = Set(a1)
			val updated = mapperDao.update(ProductEntity, selected)
			updated should be === Product(2, "blue jean", Set(a1))
			val reloaded = mapperDao.select(ProductEntity, 2).get
			reloaded should be === updated
		}

		test("update immutable entity") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			val updated = mapperDao.update(ProductEntity, selected, Product(2, "blue jean new", Set(a1)))
			updated should be === Product(2, "blue jean new", Set(a1))
			val reloaded = mapperDao.select(ProductEntity, 2).get
			reloaded should be === updated
		}

		test("lazy load") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			// use reflection to detect that the field wasn't set
			val r: Set[Attribute] = reflectionManager.get("attributes", selected)
			r should be(Set())

			verifyNotLoadded(selected)

			selected should be === Product(2, "blue jean", inserted.attributes)
			selected.attributes should be === inserted.attributes
		}

		test("manually updating them stops lazy loading") {
			createTables
			val a1 = mapperDao.insert(AttributeEntity, Attribute(6, "colour", "blue"))
			val a2 = mapperDao.insert(AttributeEntity, Attribute(9, "size", "medium"))
			val inserted = mapperDao.insert(ProductEntity, Product(2, "blue jean", Set(a1, a2)))

			val selected = mapperDao.select(SelectConfig(lazyLoad = LazyLoad.all), ProductEntity, 2).get
			// use reflection to detect that the field wasn't set
			val r: Set[Attribute] = reflectionManager.get("attributes", selected)
			r should be(Set())

			// manually updating should stop lazy loading kick in
			selected.attributes = Set(a1)
			selected.attributes should be(Set(a1))

			verifyNotLoadded(selected)
		}
	}

	def verifyNotLoadded(o: Any) {
		val persisted = o.asInstanceOf[Persisted]
		persisted.mapperDaoValuesMap.isLoaded(ProductEntity.attributes) should be(false)
		persisted.mapperDaoValuesMap.isLoaded(ProductEntity.properties) should be(false)
	}
	def verifyPropertiesNotLoadded(o: Any) {
		val persisted = o.asInstanceOf[Persisted]
		persisted.mapperDaoValuesMap.isLoaded(ProductEntity.attributes) should be(true)
		persisted.mapperDaoValuesMap.isLoaded(ProductEntity.properties) should be(false)
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}

	case class Product(val id: Int, val name: String, var attributes: Set[Attribute], val properties: Set[Property] = Set())
	case class Attribute(val id: Int, val name: String, val value: String)
	case class Property(val id: Int, val name: String, val value: String)

	object ProductEntity extends Entity[IntId, Product] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) getter ("attributes") to (_.attributes)
		val properties = manytomany(PropertyEntity) getter ("properties") to (_.properties)
		def constructor(implicit m) = new Product(id, name, attributes, properties) with IntId
	}

	object AttributeEntity extends Entity[IntId, Attribute] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with IntId
	}

	object PropertyEntity extends Entity[IntId, Property] {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Property(id, name, value) with IntId
	}
}