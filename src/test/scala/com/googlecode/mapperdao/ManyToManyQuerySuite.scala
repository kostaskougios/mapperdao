package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.{Jdbc, Setup}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

/**
 * @author kostantinos.kougios
 *
 *         28 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyQuerySuite extends FunSuite with Matchers
{
	implicit val (jdbc: Jdbc, mapperDao: MapperDao, queryDao: QueryDao) = Setup.setupMapperDao(List(ProductEntity, AttributeEntity))

	import Query._

	val p = ProductEntity
	val attr = AttributeEntity

	test("join on") {
		createTables
		val List(a, b, c, d) = mapperDao.insertBatch(AttributeEntity,
			List(
				Attribute(100, "size", "A"),
				Attribute(101, "size", "B"),
				Attribute(102, "size", "C"),
				Attribute(103, "size", "D")
			)
		)
		val List(p1, p2, p3, _) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(1, "TV 1", Set(a, b)),
				Product(2, "TV 1", Set(a, c)),
				Product(3, "TV 3", Set(a)),
				Product(4, "TV 4", Set(d))
			)
		)

		(
			select from p
				join(p, p.attributes, attr)
				join (p as 'p1) on ('p1, p.id) <> p.id
				join(p as 'p1, p.attributes, attr as 'a1) on ('a1, attr.value) === attr.value
			).toSet should be(Set(p1, p2, p3))
	}

	test("self join") {
		createTables
		val List(a, b, c, d) = mapperDao.insertBatch(AttributeEntity,
			List(
				Attribute(100, "size", "A"),
				Attribute(101, "size", "B"),
				Attribute(102, "size", "C"),
				Attribute(103, "size", "D")
			)
		)
		val List(p1, p2, _, _) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(1, "TV 1", Set(a, b)),
				Product(2, "TV 1", Set(c, d)),
				Product(3, "TV 3", Set(a, c)),
				Product(4, "TV 4", Set(d))
			)
		)

		(
			select from p
				join (p as 'p1) on p.name ===('p1, p.name) and p.id <>('p1, p.id)
			).toSet should be(Set(p1, p2))

		(
			select from p
				join (p as 'p1) on p.name ===('p1, p.name) and p.id <>('p1, p.id)
				join(p as 'p1, p.attributes, attr as 'a1)
				where ('a1, attr.value) === "D"
			).toSet should be(Set(p1))
	}

	test("match on FK") {
		createTables
		val List(a, b, c, d) = mapperDao.insertBatch(AttributeEntity,
			List(
				Attribute(100, "size", "A"),
				Attribute(101, "size", "B"),
				Attribute(102, "size", "C"),
				Attribute(103, "size", "D")
			)
		)
		val List(p1, p2, p3, p4) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(1, "TV 1", Set(a, b)),
				Product(2, "TV 2", Set(c, d)),
				Product(3, "TV 3", Set(a, c)),
				Product(4, "TV 4", Set(d))
			)
		)

		def q(attr: Attribute) = select from p where p.attributes === attr

		def qn(attr: Attribute) = select from p where p.attributes <> attr

		q(a).toSet should be(Set(p1, p3))
		q(d).toSet should be(Set(p2, p4))
		q(c).toSet should be(Set(p2, p3))
		q(d).toSet should be(Set(p2, p4))
		qn(d).toSet should be(Set(p1, p2, p3))
	}

	test("query with limits (offset only)") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		val qc = QueryConfig(offset = Some(2))
		(select from p).toList(qc).toSet should be === Set(p2, p3)
	}

	test("query with skip") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		(select from p join(p, p.attributes, attr) where attr.value === "46'")
			.toList(QueryConfig(skip = Set(ProductEntity.attributes)))
			.toSet should be === Set(Product(1, "TV 1", Set()), Product(3, "TV 3", Set()))
	}

	test("query with limits (limit only)") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		(select from p).toList(QueryConfig(limit = Some(2))).toSet should be === Set(p0, p1)
	}

	test("query with limits") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		(select from p).toList(QueryConfig(offset = Some(1), limit = Some(2))).toSet should be === Set(p1, p2)
	}

	test("order by") {
		createTables
		val a = mapperDao.insert(AttributeEntity, Attribute(100, "size", "A"))
		val b = mapperDao.insert(AttributeEntity, Attribute(101, "size", "B"))
		val c = mapperDao.insert(AttributeEntity, Attribute(102, "size", "C"))
		val d = mapperDao.insert(AttributeEntity, Attribute(103, "size", "D"))

		val p1 = mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a, b)))
		val p2 = mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(c, d)))
		val p3 = mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a, c)))
		val p4 = mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(d)))

		val result = (
			select from p
				join(p, p.attributes, attr)
				order by(attr.value, asc, p.id, desc)
			).toList
		result should be === List(p3, p1, p1, p3, p2, p4, p2)
	}

	test("join, 1 condition") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		val p2 = mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		(select from p join(p, p.attributes, attr) where attr.value === "46'").toList.toSet should be === Set(p0, p2)
	}

	test("join, 2 conditions") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))

		val p0 = mapperDao.insert(ProductEntity, Product(1, "TV 1", Set(a0, a2)))
		val p1 = mapperDao.insert(ProductEntity, Product(2, "TV 2", Set(a1, a2)))
		mapperDao.insert(ProductEntity, Product(3, "TV 3", Set(a0, a3)))
		val p3 = mapperDao.insert(ProductEntity, Product(4, "TV 4", Set(a1, a3)))

		(select from p join(p, p.attributes, attr) where attr.value === "50'" or attr.value === "black").toList.toSet should be === Set(p0, p1, p3)
	}

	def createTables = {
		Setup.dropAllTables(jdbc)
		jdbc.update( """
					create table Product (
						id int not null,
						name varchar(100) not null,
						primary key(id)
					)
					 """)
		jdbc.update( """
					create table Attribute (
						id int not null,
						name varchar(100) not null,
						value varchar(100) not null,
						primary key(id)
					)
					 """)
		jdbc.update( """
					create table Product_Attribute (
						product_id int not null,
						attribute_id int not null,
						primary key(product_id,attribute_id)
					)
					 """)
	}

	case class Product(id: Int, name: String, attributes: Set[Attribute])

	case class Attribute(id: Int, name: String, value: String)

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m: ValuesMap) = new Product(id, name, attributes) with Stored
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m: ValuesMap) = new Attribute(id, name, value) with Stored
	}

}