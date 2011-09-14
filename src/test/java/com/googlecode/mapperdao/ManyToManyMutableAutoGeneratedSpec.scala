package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.jdbc.Setup
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
class ManyToManyMutableAutoGeneratedSpec extends SpecificationWithJUnit {
	import ManyToManyMutableAutoGeneratedSpec._

	val (jdbc, mapperDao) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	"update relationship of leaf node" in {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue", Set()))
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(a1, Attribute("size", "medium", Set()))))

		val sa1 = mapperDao.select(AttributeEntity, a1.id).get
		sa1.products.clear
		val ua1 = mapperDao.update(AttributeEntity, sa1)
		ua1 must_== Attribute("colour", "blue", Set())

		mapperDao.select(AttributeEntity, ua1.id).get must_== ua1
		mapperDao.select(ProductEntity, inserted.id).get must_== Product("blue jean", Set(Attribute("size", "medium", Set())))

		ua1.products += inserted
		val ua2 = mapperDao.update(AttributeEntity, sa1)
		mapperDao.select(AttributeEntity, ua2.id).get must_== ua2
		mapperDao.select(ProductEntity, inserted.id).get must_== Product("blue jean", Set(ua2, Attribute("size", "medium", Set())))
	}

	"update value of leaf node" in {
		createTables
		val a1 = mapperDao.insert(AttributeEntity, Attribute("colour", "blue", Set()))
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(a1, Attribute("size", "medium", Set()))))

		val sa1 = mapperDao.select(AttributeEntity, a1.id).get
		sa1.value = "red"
		val ua1 = mapperDao.update(AttributeEntity, sa1)
		ua1 must_== Attribute("colour", "red", Set(inserted))

		mapperDao.select(AttributeEntity, ua1.id).get must_== ua1
	}

	"update tree of entities, remove entity from set" in {
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
		mapperDao.select(ProductEntity, updated.id) must beNone
	}

	"update tree of entities, add new entities to set" in {
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
		mapperDao.select(ProductEntity, updated.id) must beNone
	}

	"update tree of entities, add persisted entity to set" in {
		createTables
		val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()))))

		val persistedA = mapperDao.insert(AttributeEntity, Attribute("size", "medium", Set()))

		inserted.name = "just jean"
		inserted.attributes += persistedA
		inserted.attributes += Attribute("size", "large", Set())
		val updated = mapperDao.update(ProductEntity, inserted)
		test(inserted, updated)

		val selected = mapperDao.select(ProductEntity, mapperDao.intIdOf(updated)).get
		test(updated, selected)

		mapperDao.delete(ProductEntity, updated)
		mapperDao.select(ProductEntity, updated.id) must beNone
	}

	"insert tree of entities (cyclic references) with persisted leaf entities" in {
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
			selected.products.head must_== inserted
		}

		{
			val inserted2 = mapperDao.insert(ProductEntity, Product("t-shirt", HashSet(a2)))
			val selected = mapperDao.select(AttributeEntity, a2.id).get
			selected.products.toSet must_== Set(inserted, inserted2)
		}

		mapperDao.delete(ProductEntity, inserted)
		mapperDao.select(ProductEntity, inserted.id) must beNone
	}

	"randomize id's and select" in {
		createTables
		val l = for (i <- 1 to 5) yield {
			val product = Product("blue jean" + i, Set(Attribute("colour" + i, "blue" + i, Set()), Attribute("size" + i * 2, "medium" + i * 2, Set())))
			mapperDao.insert(ProductEntity, product)
		}
		mapperDao.select(ProductEntity, l.last.id).get must_== l.last
		mapperDao.select(ProductEntity, l.head.id).get must_== l.head
	}

	"randomize id's, update and select" in {
		createTables
		val l = for (i <- 1 to 5) yield {
			val product = Product("blue jean" + i, Set(Attribute("colour" + i, "blue" + i, Set()), Attribute("size" + i * 2, "medium" + i * 2, Set())))
			mapperDao.insert(ProductEntity, product)
		}
		val updated = mapperDao.update(ProductEntity, l.last, Product("blue jeanX", l.last.attributes.filterNot(_.name == "colour")))
		mapperDao.select(ProductEntity, l.last.id).get must_== updated
		mapperDao.select(ProductEntity, l.head.id).get must_== l.head
	}

	def test(expected: Product, actual: Product) =
		{
			actual.attributes.toSet must_== expected.attributes.toSet
			actual must_== expected
		}

	def test(expected: Attribute, actual: Attribute) =
		{
			actual.products.toSet must_== expected.products.toSet
			actual must_== expected
		}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			jdbc.update("""
					create table Product (
						id serial not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Attribute (
						id serial not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id),
						foreign key(product_id) references Product(id) on delete cascade,
						foreign key(attribute_id) references Attribute(id) on delete cascade
					)
			""")
		}
}

object ManyToManyMutableAutoGeneratedSpec {
	case class Product(var name: String, var attributes: Set[Attribute]) {
		override def equals(o: Any): Boolean = o match {
			case p: Product => name == p.name
			case _ => false
		}

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

		override def toString: String = "Attribute(%s,%s)".format(name, value)
	}

	object ProductEntity extends Entity[IntId, Product](classOf[Product]) {
		val id = autoGeneratedPK("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany(classOf[Attribute], _.attributes)

		val constructor = (m: ValuesMap) => new Product(m(name), m.mutableHashSet(attributes)) with IntId with Persisted {
			val valuesMap = m
			val id = m.int(ProductEntity.id) // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}

	object AttributeEntity extends Entity[IntId, Attribute](classOf[Attribute]) {
		val id = autoGeneratedPK("id", _.id)
		val name = string("name", _.name)
		val value = string("value", _.value);
		val products = manyToManyReverse(classOf[Product], _.products)

		val constructor = (m: ValuesMap) => new Attribute(m(name), m(value), m.mutableHashSet(products)) with Persisted with IntId {
			val valuesMap = m
			val id = m.int(AttributeEntity.id) // we explicitly convert this to an int because mysql serial values are always BigInteger (a bug maybe?)
		}
	}
}