package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 *         30 May 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyCustomLoaderSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	if (Setup.database == "h2") {
		val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(List(ProductEntity, AttributeEntity))

		val loaders = List(
			CustomLoader(
				ProductEntity.attributes,
				(selectConfig: SelectConfig, values: List[JdbcMap]) =>
					selectConfig.data match {
						case Some('replaced) =>
							List(
								new Attribute("x", "y") with SurrogateIntId
								{
									val id = 5
								}
							)
						case _ =>
							values.map {
								v =>
									val id = v.int("attribute_id")
									val a = AttributeEntity
									import Query._
									queryDao.querySingleResult(select from a where a.id === id).get
							}
					}
			)
		)

		test("load related using custom loader") {
			createProductAttribute(jdbc)
			val inserted = mapperDao.insert(ProductEntity, Product("p1", Set(Attribute("a1", "v1"), Attribute("a2", "v2"))))
			val selected = mapperDao.select(SelectConfig(
				data = Some('replaced),
				manyToManyCustomLoaders = loaders

			), ProductEntity, inserted.id).get
			selected should be === Product("p1", Set(Attribute("x", "y")))
		}

		test("update custom loaded data") {
			createProductAttribute(jdbc)
			val inserted = mapperDao.insert(ProductEntity, Product("p1", Set(Attribute("a1", "v1"), Attribute("a2", "v2"))))
			val selected = mapperDao.select(SelectConfig(
				manyToManyCustomLoaders = loaders

			), ProductEntity, inserted.id).get
			val up = Product("updated", selected.attributes + Attribute("a3", "x3"))
			val updated = mapperDao.update(ProductEntity, selected, up)
			updated should be === up
			mapperDao.select(ProductEntity, inserted.id).get should be === updated
		}
	}
}