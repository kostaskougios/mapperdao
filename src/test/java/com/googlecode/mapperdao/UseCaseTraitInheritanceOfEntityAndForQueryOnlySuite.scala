package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 23 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class UseCaseTraitInheritanceOfEntityAndForQueryOnlySuite extends FunSuite with ShouldMatchers {

	import UseCaseTraitInheritanceOfEntityAndForQueryOnlySuite._

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(TagEntity, SimpleProductEntity))

		test("crud") {
			createTables()
			mapperDao.insert(TagEntity, Tag("laptop", Product("lapt100", "SuperFast 1000")))
		}

		def createTables() =
			{
				Setup.dropAllTables(jdbc)
				Setup.queries(this, jdbc).update("ddl")
			}
	}
}

object UseCaseTraitInheritanceOfEntityAndForQueryOnlySuite {
	case class Tag(tag: String, product: Product)
	case class Product(refCode: String, name: String)

	object TagEntity extends SimpleEntity[Tag] {
		val tag = column("tag") to (_.tag)
		val product = manytoone(SimpleProductEntity) to (_.product)

		declarePrimaryKey(tag)
		declarePrimaryKey(product)

		def constructor(implicit m: ValuesMap) = new Tag(tag, product) with Persisted
	}

	trait ProductEntity[T <: Product] extends SimpleEntity[T] {
		val name = column("name") to (_.name)
	}

	object SimpleProductEntity extends ProductEntity[Product] {

		val refCode = key("refCode") to (_.refCode)
		val tags = onetomany(TagEntity) forQueryOnly () to (ce => Nil)

		def constructor(implicit m: ValuesMap) = new Product(refCode, name) with Persisted
	}
}