package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         22 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class ForQueryOnlySuite extends FunSuite with Matchers
{

	if (Setup.database == "h2") {
		import CommonEntities._

		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, AttributeEntity))

		val ae = AttributeEntity
		val pe = ProductEntity

		def createData = {
			createProductAttribute(jdbc)

			// create test data
			val red = mapperDao.insert(AttributeEntity, Attribute("colour", "red"))
			val blue = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
			val acer = mapperDao.insert(AttributeEntity, Attribute("brand", "acer"))
			val dell = mapperDao.insert(AttributeEntity, Attribute("brand", "dell"))

			val p1 = mapperDao.insert(ProductEntity, Product("a red product", Set(red)))
			val p2 = mapperDao.insert(ProductEntity, Product("a blue product", Set(red, blue)))
			val p3 = mapperDao.insert(ProductEntity, Product("an acer blue product", Set(acer, blue)))
			val p4 = mapperDao.insert(ProductEntity, Product("a dell blue product", Set(dell, blue)))
			val p5 = mapperDao.insert(ProductEntity, Product("a dell product in red and blue", Set(dell, red, blue)))
			(red, blue, acer, dell, p1, p2, p3, p4, p5)
		}

		test("simple query with join on 2") {
			val (red, blue, _, dell, _, _, _, p4, p5) = createData
			import Query._
			(
				select
					from ae
					where
					ae.product === p4 or ae.product === p5
				).toSet(queryDao) should be(Set(dell, red, blue))
		}

		test("simple query with join") {
			val (_, blue, _, dell, _, _, _, p4, _) = createData

			import Query._
			(
				select
					from ae
					where
					ae.product === p4
				).toSet(queryDao) should be(Set(dell, blue))
		}

		test("complex query with join") {
			val (red, blue, _, _, _, _, _, _, _) = createData
			import Query._

			(
				select
					from ae
					join(ae, ae.product, pe)
					where
					ae.name === "colour"
					and (pe.name like "%dell%")
				).toSet(queryDao) should be(Set(red, blue))
		}
	}
}
