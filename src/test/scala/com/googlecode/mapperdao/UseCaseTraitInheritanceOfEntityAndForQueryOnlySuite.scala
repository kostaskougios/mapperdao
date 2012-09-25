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

		val te = TagEntity
		val spe = SimpleProductEntity

		test("crud") {
			createTables()
			val l1 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt100", "SuperFast 1000")))
			val l2 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt101", "SlowAsHell 2000")))

			val sl1 = mapperDao.select(TagEntity, l1.tag, l1.product).get

			val ul1 = mapperDao.update(TagEntity, sl1, sl1.copy(tag = "changed"))
			ul1 should be === Tag("changed", Product("lapt100", "SuperFast 1000"))

			val rsl1 = mapperDao.select(TagEntity, ul1.tag, ul1.product).get
			rsl1 should be === ul1

			mapperDao.delete(TagEntity, rsl1)
			mapperDao.select(TagEntity, ul1.tag, ul1.product) should be(None)

			mapperDao.select(TagEntity, l2.tag, l2.product).get should be === l2
		}

		test("query forQueryOnly column") {
			createTables()
			val l1 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt100", "SuperFast 1000")))
			val l2 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt101", "SlowAsHell 2000")))
			val nt1 = mapperDao.insert(TagEntity, Tag("nettop", Product("nettp1", "Nettop 1200")))

			import Query._
			(
				select
				from spe
				where spe.tags === l1
			).toSet(queryDao) should be === Set(l1.product)
			(
				select
				from spe
				join (spe, spe.tags, te)
				where te.tag === "laptop"
			).toSet(queryDao) should be === Set(l1.product, l2.product)
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

	object TagEntity extends Entity[StringId, Tag] {
		val tag = key("tag") to (_.tag)
		val product = manytoone(SimpleProductEntity) to (_.product)

		declarePrimaryKey(product)

		def constructor(implicit m: ValuesMap) = new Tag(tag, product) with StringId
	}

	trait ProductEntity[T <: Product] extends Entity[StringId, T] {
		val name = column("name") to (_.name)
	}

	object SimpleProductEntity extends ProductEntity[Product] {

		val refCode = key("refCode") to (_.refCode)
		val tags = onetomany(TagEntity) forQueryOnly () to (ce => Nil)

		def constructor(implicit m: ValuesMap) = new Product(refCode, name) with StringId
	}
}