package com.googlecode.mapperdao

import com.googlecode.mapperdao.internal.EntityMap
import com.googlecode.mapperdao.jdbc.impl.MapperDaoImpl
import com.googlecode.mapperdao.jdbc.{DatabaseValues, Setup}
import com.googlecode.mapperdao.utils.Helpers
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
/**
 * @author kkougios
 */
@RunWith(classOf[JUnitRunner])
class RelatedAreLoadedFromDatabaseValuesSuite extends FunSuite with BeforeAndAfter
{

	import CommonEntities._

	val (jdbc, mapperDao: MapperDaoImpl, queryDao) = Setup.setupMapperDao(AllEntities)

	before {
		Setup.dropAllTables(jdbc)
	}

	test("many-to-many") {
		// will not create tables to make sure all entities are loaded from DatabaseValues
		val ids = Helpers.idToList(5)
		val dbVs = new DatabaseValues(Map(
			"id" -> 5,
			"name" -> "product1"
		), Map(
			ProductEntity.attributes.column.aliasLowerCase -> (new DatabaseValues(
				Map(
					"id" -> 105,
					"name" -> "a1",
					"value" -> "v1"
				)
			) :: new DatabaseValues(
				Map(
					"id" -> 106,
					"name" -> "a2",
					"value" -> "v2"
				)
			) :: Nil)
		))
		val s = mapperDao.selectInner(ProductEntity, SelectConfig.Default, ids, new EntityMap, Some(dbVs)).get
		s should be(Product("product1", Set(Attribute("a1", "v1"), Attribute("a2", "v2"))))
		Helpers.intIdOf(s) should be(5)
		Helpers.intIdOf(s.attributes.head) should be(105)
		Helpers.intIdOf(s.attributes.tail.head) should be(106)
	}

	test("many-to-one") {
		val ids = Helpers.idToList(10)

		val dbVs = new DatabaseValues(Map(
			"id" -> 10,
			"name" -> "person1",
			"company_id" -> 101
		),
			Map(
				PersonEntity.company.column.aliasLowerCase -> (
					new DatabaseValues(
						Map(
							"id" -> 101,
							"name" -> "company1"
						)
					) :: Nil
					)
			)
		)
		val r = mapperDao.selectInner(PersonEntity, SelectConfig.Default, ids, new EntityMap, Some(dbVs)).get
		r should be(Person("person1", Company("company1")))
		Helpers.intIdOf(r) should be(10)
		Helpers.intIdOf(r.company) should be(101)
	}

	test("one-to-many") {
		val ids = Helpers.idToList(20)
		val dbVs = new DatabaseValues(Map(
			"id" -> 20,
			"name" -> "owner1"
		), Map(
			OwnerEntity.owns.column.aliasLowerCase -> (
				new DatabaseValues(
					Map(
						"id" -> 201,
						"address" -> "first address",
						"owner_id" -> 20
					)
				) :: new DatabaseValues(
					Map(
						"id" -> 202,
						"address" -> "second address",
						"owner_id" -> 20
					)
				) :: Nil
				)
		)
		)
		val r = mapperDao.selectInner(OwnerEntity, SelectConfig.Default, ids, new EntityMap, Some(dbVs)).get
		r should be(Owner("owner1", Set(House("first address"), House("second address"))))
		Helpers.intIdOf(r) should be(20)
		Helpers.intIdOf(r.owns.head) should be(201)
		Helpers.intIdOf(r.owns.tail.head) should be(202)
	}

	//TODO: enable this test

	//	test("one-to-one") {
	//		val ids = Helpers.idToList(30)
	//		val dbVs = new DatabaseValues(Map(
	//			"id" -> 301,
	//			"name" -> "wife",
	//			"age" -> 19
	//		), Map(
	//			WifeEntity.husband.column.aliasLowerCase -> (
	//				new DatabaseValues(
	//					Map(
	//						"name" -> "husband",
	//						"age" -> 20,
	//						"wife_id" -> 301
	//					)
	//				) :: Nil
	//				)
	//		)
	//		)
	//		val r = mapperDao.selectInner(WifeEntity, SelectConfig.default, ids, new EntityMap, Some(dbVs)).get
	//		r should be(Wife("wife", 19, Husband("husband", 20, null)))
	//		Helpers.intIdOf(r) should be(301)
	//	}
}
