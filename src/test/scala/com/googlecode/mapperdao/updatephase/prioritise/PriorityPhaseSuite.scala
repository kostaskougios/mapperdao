package com.googlecode.mapperdao.updatephase.prioritise

import org.junit.runner.RunWith
import org.scalatest.{Matchers, FunSuite}
import org.scalatest.junit.JUnitRunner
import com.googlecode.mapperdao.{UpdateConfig, CommonEntities}

/**
 * @author kostantinos.kougios
 *
 *         15 Dec 2012
 */
@RunWith(classOf[JUnitRunner])
class PriorityPhaseSuite extends FunSuite with Matchers
{

	import CommonEntities._

	test("prioritise, many-to-many") {
		val pf = new PriorityPhase(UpdateConfig.default)
		val pri = pf.prioritiseType(ProductEntity.tpe)

		pri should be(List(ProductEntity.tpe, AttributeEntity.tpe))
	}

	test("prioritise, many-to-one") {
		val pf = new PriorityPhase(UpdateConfig.default)
		val pri = pf.prioritiseType(PersonEntity.tpe)

		pri should be(List(CompanyEntity.tpe, PersonEntity.tpe))
	}

	test("prioritise, one-to-many") {
		val pf = new PriorityPhase(UpdateConfig.default)
		val pri = pf.prioritiseType(OwnerEntity.tpe)

		pri should be(List(OwnerEntity.tpe, HouseEntity.tpe))
	}
}
