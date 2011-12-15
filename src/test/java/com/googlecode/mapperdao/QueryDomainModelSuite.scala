package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 15 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class QueryDomainModelSuite extends FunSuite with ShouldMatchers {

	case class Product(val id: Int, val name: String, val attributes: Set[Attribute])
	case class Attribute(val id: Int, val name: String, val value: String)

	object ProductEntity extends SimpleEntity(classOf[Product]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val attributes = manytomany(AttributeEntity) to (_.attributes)
		def constructor(implicit m) = new Product(id, name, attributes) with Persisted
	}

	object AttributeEntity extends SimpleEntity(classOf[Attribute]) {
		val id = key("id") to (_.id)
		val name = column("name") to (_.name)
		val value = column("value") to (_.value)

		def constructor(implicit m) = new Attribute(id, name, value) with Persisted
	}

	val prod = ProductEntity
	val attr = AttributeEntity
	import Query._

	test("entity must be set") {
		(select from prod).entity should equal(prod)
	}

	test("join") {
		val q = select from prod join (prod, prod.attributes, attr)
		val j = q.joins.head
		j.joinEntity should equal(prod)
		j.foreignEntity should equal(attr)
		j.column should equal(prod.attributes.column)
	}

}
