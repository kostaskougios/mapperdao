package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyMutableAutoGeneratedSuite extends FunSuite with ShouldMatchers {
	import ManyToManyMutableAutoGeneratedSpec._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	test("update relationship of leaf node") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue", Set()))
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(a1, Attribute("size", "medium", Set()))))

		val sa1 = mapperDao.select(AttributeEntity, a1.id).get
		sa1.products.clear
		val ua1 = mapperDao.update(AttributeEntity, sa1)
		ua1 should be === Attribute("colour", "blue", Set())

		mapperDao.select(AttributeEntity, ua1.id).get should be === ua1
		mapperDao.select(ProductEntity, inserted.id).get should be === Product("blue jean", Set(Attribute("size", "medium", Set())))

		ua1.products += inserted
		val ua2 = mapperDao.update(AttributeEntity, sa1)
		mapperDao.select(AttributeEntity, ua2.id).get should be === ua2
		mapperDao.select(ProductEntity, inserted.id).get should be === Product("blue jean", Set(ua2, Attribute("size", "medium", Set())))
	}

	test("update value of leaf node") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue", Set()))
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(a1, Attribute("size", "medium", Set()))))

		val sa1 = mapperDao.select(AttributeEntity, a1.id).get
		sa1.value = "red"
		val ua1 = mapperDao.update(AttributeEntity, sa1)
		ua1 should be === Attribute("colour", "red", Set(inserted))

		mapperDao.select(AttributeEntity, ua1.id).get should be === ua1
	}

	test("update tree of entities, remove entity from set") {
		createTables
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()), Attribute("size", "medium", Set()), Attribute("size", "large", Set()))))

		inserted.name = "Changed"
		inserted.attributes -= Attribute("size", "medium", Set())
		inserted.attributes -= Attribute("size", "large", Set())

		val updated = mapperDao.update(ProductEntity, inserted)
		test(inserted, updated)

		val selected = mapperDao.select(ProductEntity, updated.id).get
		test(updated, selected)

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) should be(None)
	}

	test("update tree of entities, add new entities to set") {
		createTables
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()))))

		inserted.name = "just jean"
		inserted.attributes += Attribute("size", "medium", Set())
		inserted.attributes += Attribute("size", "large", Set())
		val updated = mapperDao.update(ProductEntity, inserted)
		test(inserted, updated)

		val selected = mapperDao.select(ProductEntity, updated.id).get
		test(updated, selected)

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) should be(None)
	}

	test("update tree of entities, add persisted entity to set") {
		createTables
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()))))

		val persistedA = mapperDao.insert(AttributeEntity, Attribute("size", "medium", Set()))

		inserted.name = "just jean"
		inserted.attributes += persistedA
		inserted.attributes += Attribute("size", "large", Set())
		val updated = mapperDao.update(ProductEntity, inserted)
		test(inserted, updated)

		val selected = mapperDao.select(ProductEntity, Helpers.intIdOf(updated)).get
		test(updated, selected)

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) should be(None)
	}

	test("insert tree of entities, cyclic references,  leaf entities") {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue", Set()))
		val a2 = mapperDao.insert(AttributeEntity, Attribute("size", "medium", Set()))
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", HashSet(a1, a2)))

		{
			// due to cyclic reference, the attributes collection contains "mock" products
			val selected = mapperDao.select(ProductEntity, inserted.id).get
			test(inserted, selected)
		}

		{
			val selected = mapperDao.select(AttributeEntity, a1.id).get
			selected.products.head should be === inserted
		}

		{
			val inserted2 = mapperDao.insert(ProductEntity, Product("t-shirt", HashSet(a2)))
			val selected = mapperDao.select(AttributeEntity, a2.id).get
			selected.products should be === Set(inserted2, inserted)
		}

		mapperDao.delete(ProductEntity, inserted)
		mapperDao.select(ProductEntity, inserted.id) should be(None)
	}

	test("randomize id's and select") {
		createTables
		val l = for (i <- 1 to 5) yield {
			val product = Product("blue jean" + i, Set(Attribute("colour" + i, "blue" + i, Set()), Attribute("size" + i * 2, "medium" + i * 2, Set())))
			mapperDao.insert(ProductEntity, product)
		}
		mapperDao.select(ProductEntity, l.last.id).get should be === l.last
		mapperDao.select(ProductEntity, l.head.id).get should be === l.head
	}

	test("randomize id's, update and select") {
		createTables
		val l = for (i <- 1 to 5) yield {
			val product = Product("blue jean" + i, Set(Attribute("colour" + i, "blue" + i, Set()), Attribute("size" + i * 2, "medium" + i * 2, Set())))
			mapperDao.insert(ProductEntity, product)
		}
		val updated = mapperDao.update(ProductEntity, l.last, Product("blue jeanX", l.last.attributes.filterNot(_.name == "colour")))
		mapperDao.select(ProductEntity, l.last.id).get should be === updated
		mapperDao.select(ProductEntity, l.head.id).get should be === l.head
	}

	def test(expected: Product, actual: Product) =
		{
			actual.attributes.toSet should be === expected.attributes.toSet
			actual should be === expected
		}

	def test(expected: Attribute, actual: Attribute) =
		{
			actual.products.toSet should be === expected.products.toSet
			actual should be === expected
		}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
			Setup.database match {
				case "oracle" =>
					Setup.createSeq(jdbc, "ProductSeq")
					Setup.createSeq(jdbc, "AttributeSeq")
				case _ =>
			}
		}
}

object ManyToManyMutableAutoGeneratedSpec {
	case class Product(var name: String, var attributes: Set[Attribute]) {
		override def equals(o: Any): Boolean = o match {
			case p: Product => name == p.name
			case _ => false
		}
		override def hashCode = name.hashCode
		override def toString: String = "Product(%s)".format(name)
	}
	case class Attribute(var name: String, var value: String, var products: Set[Product]) {
		/**
		 * since there are cyclic references, we override equals
		 * to avoid StackOverflowError
		 */
		override def equals(o: Any): Boolean = o match {
			case a: Attribute => a.name == name && a.value == value
			case _ => false
		}

		override def hashCode = name.hashCode + value.hashCode
		override def toString: String = "Attribute(%s,%s)".format(name, value)
	}

	object ProductEntity extends Entity[Int, SurrogateIntId, Product] {
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("ProductSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m) = new Product(name, m.mutableHashSet(attributes)) with SurrogateIntId {
			val id: Int = ProductEntity.id
		}
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute] {
		val id = key("id") sequence (Setup.database match {
			case "oracle" => Some("AttributeSeq")
			case _ => None
		}) autogenerated (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)
		val products = manytomanyreverse(ProductEntity) to (_.products)

		def constructor(implicit m) = new Attribute(name, value, m.mutableHashSet(products)) with SurrogateIntId {
			val id: Int = AttributeEntity.id
		}
	}
}