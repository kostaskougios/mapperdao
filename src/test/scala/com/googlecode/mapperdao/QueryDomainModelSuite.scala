package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         15 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class QueryDomainModelSuite extends FunSuite with ShouldMatchers
{

	case class Product(id: Int, name: String, attributes: Set[Attribute])

	case class Attribute(id: Int, name: String, value: String)

	object ProductEntity extends Entity[Int, SurrogateIntId, Product]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)

		def constructor(implicit m) = new Product(id, name, attributes) with Stored
	}

	object AttributeEntity extends Entity[Int, SurrogateIntId, Attribute]
	{
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with Stored
	}

	val prod = ProductEntity
	val attr = AttributeEntity

	import Query._

	test("query entity must be set") {
		(select from prod).entity should equal(prod)
	}

	test("join, joinEntity, foreignEntity, column") {
		val q = select from prod join(prod, prod.attributes, attr)
		val j = q.joins.head match {
			case Query.Join(joinEntity, ci, foreignEntity) =>
				joinEntity should equal(prod)
				foreignEntity should equal(attr)
				ci.column should equal(prod.attributes.column)
		}
	}

}
