package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.jdbc.Setup

/**
 * @author	konstantinos.kougios
 *
 *            13 Sep 2012
 */
@RunWith(classOf[JUnitRunner])
class BlobSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	// blobs are not currently supported for derby
	if (Setup.database != "derby") {

		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(ImageEntity))
		val ie = ImageEntity

		test("CRUD") {
			createImage(jdbc)

			val im1 = Image("tree", Array(5, 6, 7))
			val inserted = mapperDao.insert(ImageEntity, im1)
			inserted should be === im1

			val selected = mapperDao.select(ImageEntity, inserted.id).get
			selected.data.toList should be === inserted.data.toList

			val uim1 = im1.copy(data = Array(15, 16, 17, 18))
			val updated = mapperDao.update(ImageEntity, inserted, uim1)
			updated should be === uim1

			mapperDao.select(ImageEntity, inserted.id).get.data.toList should be === uim1.data.toList

			mapperDao.delete(ImageEntity, updated)
			mapperDao.select(ImageEntity, inserted.id) should be === None
		}

		test("query") {
			createImage(jdbc)

			val im1 = Image("tree1", Array(5, 6, 7))
			val i1 = mapperDao.insert(ImageEntity, im1)
			val im2 = Image("tree2", Array(15, 16))
			val i2 = mapperDao.insert(ImageEntity, im2)

			import Query._

			val l = (
				select
					from ie
					orderBy(ie.id, asc)
				).toList(queryDao)

			l.head.data.toList should be === im1.data.toList
			l.tail.head.data.toList should be === im2.data.toList
		}
	}
}
