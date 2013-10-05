package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FunSuite}
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.utils.Helpers

/**
 * @author kostantinos.kougios
 *
 *         23 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class UseCaseTraitInheritanceOfEntityAndForQueryOnlySuite extends FunSuite with Matchers
{

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(TagEntity, SimpleProductEntity))

		val te = TagEntity
		val spe = SimpleProductEntity

		test("crud") {
			createTables()
			val l1 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt100", "SuperFast 1000")))
			val l2 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt101", "SlowAsHell 2000")))

			val sl1 = mapperDao.select(TagEntity, (l1.tag, Helpers.asNaturalStringId(l1.product))).get

			val ul1 = mapperDao.update(TagEntity, sl1, sl1.copy(tag = "changed"))
			ul1 should be === Tag("changed", Product("lapt100", "SuperFast 1000"))

			val rsl1 = mapperDao.select(TagEntity, (ul1.tag, Helpers.asNaturalStringId(ul1.product))).get
			rsl1 should be === ul1

			mapperDao.delete(TagEntity, rsl1)
			mapperDao.select(TagEntity, (ul1.tag, Helpers.asNaturalStringId(ul1.product))) should be(None)

			mapperDao.select(TagEntity, (l2.tag, Helpers.asNaturalStringId(l2.product))).get should be === l2
		}

		test("query forQueryOnly column") {
			createTables()
			val l1 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt100", "SuperFast 1000")))
			val l2 = mapperDao.insert(TagEntity, Tag("laptop", Product("lapt101", "SlowAsHell 2000")))
			mapperDao.insert(TagEntity, Tag("nettop", Product("nettp1", "Nettop 1200")))

			import Query._
			(
				select
					from spe
					where spe.tags === l1
				).toSet(queryDao) should be === Set(l1.product)
			(
				select
					from spe
					join(spe, spe.tags, te)
					where te.tag === "laptop"
				).toSet(queryDao) should be === Set(l1.product, l2.product)
		}

		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
		}
	}

	case class Tag(tag: String, product: Product)

	case class Product(refCode: String, name: String)

	object TagEntity extends Entity[(String, Product with NaturalStringId), With2Ids[String, Product with NaturalStringId], Tag]
	{
		val tag = key("tag") to (_.tag)
		val product = manytoone(SimpleProductEntity) to (_.product)

		declarePrimaryKey(product)

		def constructor(implicit m: ValuesMap) = new Tag(tag, product) with Stored
	}

	trait ProductEntity[T <: Product] extends Entity[String, NaturalStringId, T]
	{
		val name = column("name") to (_.name)
	}

	object SimpleProductEntity extends ProductEntity[Product]
	{
		val TagEntityRef = EntityRef("Tag", classOf[Tag], TagEntity)

		val refCode = key("refCode") to (_.refCode)
		val tags = onetomany(TagEntityRef) forQueryOnly() to (ce => Nil)

		def constructor(implicit m: ValuesMap) = new Product(refCode, name) with Stored
	}

}