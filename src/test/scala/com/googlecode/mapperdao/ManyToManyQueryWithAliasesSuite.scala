package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

/**
 * @author kostantinos.kougios
 *
 *         29 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyQueryWithAliasesSuite extends FunSuite with Matchers
{

	import ManyToManyQueryWithAliasesSuite._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(AttributeEntity, ProductEntity))

	import Query._

	val p = ProductEntity
	val attr = AttributeEntity

	test("join, 3 condition") {
		createTables
		val a0 = mapperDao.insert(AttributeEntity, Attribute(100, "size", "46'"))
		val a1 = mapperDao.insert(AttributeEntity, Attribute(101, "size", "50'"))
		val a2 = mapperDao.insert(AttributeEntity, Attribute(102, "colour", "black"))
		val a3 = mapperDao.insert(AttributeEntity, Attribute(103, "colour", "white"))
		val a4 = mapperDao.insert(AttributeEntity, Attribute(104, "dimensions", "100x100"))
		val a5 = mapperDao.insert(AttributeEntity, Attribute(105, "dimensions", "200x200"))

		val List(_, _, p2, _) = mapperDao.insertBatch(ProductEntity,
			List(
				Product(1, "TV 1", Set(a0, a2, a4)),
				Product(2, "TV 2", Set(a1, a2, a4)),
				Product(3, "TV 3", Set(a0, a3, a4)),
				Product(4, "TV 3", Set(a1, a3, a5))
			)
		)

		val p = ProductEntity

		val q0 = (select from p
			join(p, p.attributes, attr)
			join(p, p.attributes, attr as 'a1)
			join(p, p.attributes, attr as 'a2)
			where (attr.name === "size" and attr.value === "46'")
			and (('a1, attr.name) === "colour" and ('a1, attr.value) === "white")
			and (('a2, attr.name) === "dimensions" and ('a2, attr.value) === "100x100")
			)
		queryDao.query(q0) should be(List(p2))
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
}

object ManyToManyQueryWithAliasesSuite
{

	case class Product(id: Int, name: String, attributes: Set[Attribute])

	case class Attribute(id: Int, name: String, value: String)

	object AttributeEntity extends Entity[Int, NaturalIntId, Attribute]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m: ValuesMap) = new Attribute(id, name, value) with Stored
	}

	object ProductEntity extends Entity[Int, NaturalIntId, Product]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m: ValuesMap) = new Product(id, name, attributes) with Stored
	}

}