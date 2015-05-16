package com.googlecode.mapperdao.updatephase.prioritise

import com.googlecode.mapperdao.{CommonEntities, UpdateConfig}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         15 Dec 2012
 */
@RunWith(classOf[JUnitRunner])
class PriorityPhaseSuite extends FunSuite
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
