package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 15 Dec 2011
 */
@RunWith(classOf[JUnitRunner])
class QueryDomainModelSpec extends SpecificationWithJUnit {

}
object QueryDomainModelSpec {
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

	val p = ProductEntity
	val a = AttributeEntity
	import Query._
	def qConditionalJoin = select from p join (if (1 < 2) Some((p, p.attributes, a)) else None)
}