package com.googlecode.mapperdao.state.prioritise

import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.CommonEntities

/**
 * @author kostantinos.kougios
 *
 * 15 Dec 2012
 */
@RunWith(classOf[JUnitRunner])
class PriorityPhaseSuite extends FunSuite with ShouldMatchers {

	import CommonEntities._

	test("prioritise, many-to-many") {
		val pf = new PriorityPhase
		val pri = pf.prioritiseEntities(ProductEntity)

		pri should be(List(ProductEntity, AttributeEntity))
	}

	test("prioritise, many-to-one") {
		val pf = new PriorityPhase
		val pri = pf.prioritiseEntities(PersonEntity)

		pri should be(List(CompanyEntity, PersonEntity))
	}

	test("prioritise, one-to-many") {
		val pf = new PriorityPhase
		val pri = pf.prioritiseEntities(OwnerEntity)

		pri should be(List(OwnerEntity, HouseEntity))
	}
}
