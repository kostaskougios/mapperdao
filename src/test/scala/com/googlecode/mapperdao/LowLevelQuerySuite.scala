package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author kostantinos.kougios
 *
 *         22 Aug 2012
 */
@RunWith(classOf[JUnitRunner])
class LowLevelQuerySuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, AttributeEntity))

		test("low level query, single entity") {
			createProductAttribute(jdbc)
			val red = mapperDao.insert(AttributeEntity, Attribute("colour", "red"))
			val blue = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
			mapperDao.insert(AttributeEntity, Attribute("brand", "acer"))

			queryDao.lowLevelQuery(AttributeEntity, "select * from attribute where name=?", List("colour")).toSet should be === Set(red, blue)
		}

		test("low level query, parent entities") {
			createProductAttribute(jdbc)
			val red = mapperDao.insert(AttributeEntity, Attribute("colour", "red"))
			val blue = mapperDao.insert(AttributeEntity, Attribute("colour", "blue"))
			val acer = mapperDao.insert(AttributeEntity, Attribute("brand", "acer"))

			val p1 = mapperDao.insert(ProductEntity, Product("test1", Set(red)))
			val p2 = mapperDao.insert(ProductEntity, Product("test2", Set(red, blue)))
			val p3 = mapperDao.insert(ProductEntity, Product("test3", Set(acer, blue)))

			queryDao.lowLevelQuery(ProductEntity, """
				select p.* 
				from product p 
				inner join product_attribute pa on pa.product_id=p.id 
				inner join attribute a on pa.attribute_id = a.id
				where a.value=?
			                                      """, List("blue")).toSet should be === Set(p2, p3)
		}
	}
}

