package com.googlecode.mapperdao

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.Setup
import com.googlecode.mapperdao.CommonEntities._

/**
 * updating a tree of immutable entities is tough. this suite deals with this issue
 *
 * @author: kostas.kougios
 *          Date: 16/04/13
 */
@RunWith(classOf[JUnitRunner])
class ImmutableUpdatingTreeSuite extends FunSuite with ShouldMatchers
{
	val (jdbc, mapperDao, _) = Setup.setupMapperDao(TypeRegistry(ProductEntity, AttributeEntity))

	test("update level 2 entity without inserting a new one") {
		createProductAttribute(jdbc)

		val a1 = Attribute("a1", "v1")
		val a2 = Attribute("a2", "v2")
		val a3 = Attribute("a3", "v3")
		val p1 = Product("p1", Set(a1, a2))
		val p2 = Product("p2", Set(a2, a3))

		val List(i1, i2) = mapperDao.insertBatch(ProductEntity, List(p1, p2))

		val a2i = i1.attributes.find(_.name == "a2").get
		val a2Updated = replace(a2i, Attribute("a2 updated", "v2 updated"))

		val uAttrs = i1.attributes - a2 + a2Updated
		val up1 = i1.copy(name = "p1 updated", attributes = uAttrs)
		val u1 = mapperDao.update(ProductEntity, i1, up1)
		u1 should be(up1)

		mapperDao.select(ProductEntity, u1.id).get should be(u1)
		// a2 must have been updated
		mapperDao.select(ProductEntity, i2.id).get should be(i2.copy(attributes = Set(a2Updated, a3)))

	}
}
