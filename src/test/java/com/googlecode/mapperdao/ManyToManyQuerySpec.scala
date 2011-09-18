package com.googlecode.mapperdao

import org.specs2.mutable.SpecificationWithJUnit

import com.googlecode.mapperdao.Query._
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 * 28 Aug 2011
 */
class ManyToManyQuerySpec extends SpecificationWithJUnit {
	import ManyToManyQuerySpec._
	val (jdbc, mapperDao, queryDao) = Setup.setupQueryDao(TypeRegistry(ProductEntity, AttributeEntity))

	import mapperDao._
	import queryDao._
	import TestQueries._

	"match on FK" in {
		createTables
		val a = insert(AttributeEntity, Attribute(100, "size", "A"))
		val b = insert(AttributeEntity, Attribute(101, "size", "B"))
		val c = insert(AttributeEntity, Attribute(102, "size", "C"))
		val d = insert(AttributeEntity, Attribute(103, "size", "D"))

		val p1 = insert(ProductEntity, Product(1, "TV 1", Set(a, b)))
		val p2 = insert(ProductEntity, Product(2, "TV 2", Set(c, d)))
		val p3 = insert(ProductEntity, Product(3, "TV 3", Set(a, c)))
		val p4 = insert(ProductEntity, Product(4, "TV 4", Set(d)))

		query(q2(a)).toSet must_== Set(p1, p3)
		query(q2(d)).toSet must_== Set(p2, p4)
		query(q2(c)).toSet must_== Set(p2, p3)
		query(q2(d)).toSet must_== Set(p2, p4)
	}

	"order by" in {
		createTables
		val a = insert(AttributeEntity, Attribute(100, "size", "A"))
		val b = insert(AttributeEntity, Attribute(101, "size", "B"))
		val c = insert(AttributeEntity, Attribute(102, "size", "C"))
		val d = insert(AttributeEntity, Attribute(103, "size", "D"))

		val p1 = insert(ProductEntity, Product(1, "TV 1", Set(a, b)))
		val p2 = insert(ProductEntity, Product(2, "TV 2", Set(c, d)))
		val p3 = insert(ProductEntity, Product(3, "TV 3", Set(a, c)))
		val p4 = insert(ProductEntity, Product(4, "TV 4", Set(d)))

		val result = query(q0OrderBy)
		result must_== List(p3, p1, p1, p3, p2, p4, p2)
	}

	"join, 1 condition" in {
		createTables
		val a0 = insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		query(q0).toSet must_== Set(p0, p2)
	}

	"join, 2 condition" in {
		createTables
		val a0 = insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		query(q1).toSet must_== Set(p0, p1, p3)
	}

	def createTables =
		{
			Setup.dropAllTables(jdbc)
			jdbc.update("""
					create table Product (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Attribute (
						id int not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
			""")
			jdbc.update("""
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id)
					)
			""")
		}
}

object ManyToManyQuerySpec {
	object TestQueries {
		val p = ProductEntity
		val a = AttributeEntity

		import Query._

		def q0 = select from p join (p, p.attributes, a) where a.value === "46'"

		def q1 = select from p join (p, p.attributes, a) where a.value === "50'" or a.value === "black"

		def q0OrderBy = select from p join (p, p.attributes, a) orderBy (a.value, asc, p.id, desc)

		def q2(attr: Attribute) = (
			select from p
			where p.attributes === attr
		)
	}

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity(classOf[Product]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val attributes = manyToMany(classOf[Attribute], _.attributes)

		val constructor = (m: ValuesMap) => new Product(m(id), m(name), m(attributes).toSet) with Persisted {
			val valuesMap = m
		}
	}

	object AttributeEntity extends SimpleEntity(classOf[Attribute]) {
		val id = pk("id", _.id)
		val name = string("name", _.name)
		val value = string("value", _.value)

		val constructor = (m: ValuesMap) => new Attribute(m(id), m(name), m(value)) with Persisted {
			val valuesMap = m
		}
	}
}