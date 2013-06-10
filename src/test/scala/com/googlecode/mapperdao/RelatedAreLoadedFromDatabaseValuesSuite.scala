package com.googlecode.mapperdao

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.{DatabaseValues, Setup}
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.internal.EntityMap

/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class RelatedAreLoadedFromDatabaseValuesSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	val (jdbc, mapperDao: MapperDaoImpl, queryDao) = Setup.setupMapperDao(AllEntities)

	test("many-to-many") {
		val ids = Helpers.idToList(5)
		val dbVs = new DatabaseValues(Map(
			"id" -> 5,
			"name" -> "product1"
		))
		mapperDao.selectInner(ProductEntity, SelectConfig.default, ids, new EntityMap, Some(dbVs))
	}
}
