package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author	konstantinos.kougios
 *
 * 13 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class BlobSuite extends FunSuite with ShouldMatchers {
	import CommonEntities._

	val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ImageEntity))

	test("CRUD") {
		createImage(jdbc)

		val im1 = Image("tree", Array(5, 6, 7))
		val inserted = mapperDao.insert(ImageEntity, im1)
		inserted should be === im1

		val selected = mapperDao.select(ImageEntity, inserted.id).get
		selected.data.toList should be === inserted.data.toList
	}

}
