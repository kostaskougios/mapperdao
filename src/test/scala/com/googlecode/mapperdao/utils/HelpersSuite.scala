package com.googlecode.mapperdao.utils

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         26 Oct 2011
 */
@RunWith(classOf[JUnitRunner])
class HelpersSuite extends FunSuite with ShouldMatchers
{

	import Helpers._

	case class T(i: Int)

	test("added (set)") {
		val origT1 = T(1)
		val moded = merge(Set(origT1), Set(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded.filter(_.eq(origT1)).size should be === 1
		// and make sure the set contains all items
		moded should be === Set(T(1), T(2), T(3))
	}

	test("removed (set)") {
		merge(Set(T(1)), Set(T(2), T(3))) should be === Set(T(2), T(3))
	}

	test("added (list)") {
		val origT1 = T(1)
		val moded = merge(List(origT1), List(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) should be theSameInstanceAs (origT1)
		// and make sure the set contains all items
		moded should be === List(T(1), T(2), T(3))
	}

	test("added different instance but equals, keep 1 only (list)") {
		val origT1 = T(1)
		val origT1Second = T(1)
		val moded = merge(List(origT1, origT1Second), List(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) should be theSameInstanceAs (origT1)
		// and make sure the set contains all items
		moded should be === List(T(1), T(2), T(3))
	}

	test("added different instance but equals, keep both (list)") {
		val origT1 = T(1)
		val origT1Second = T(1)
		val moded = merge(List(origT1, origT1Second), List(T(1), T(2), T(1), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) should be theSameInstanceAs (origT1)
		moded(2) should be theSameInstanceAs (origT1Second)
		// and make sure the set contains all items
		moded should be === List(T(1), T(2), T(1), T(3))
	}

	test("removed (list)") {
		val moded = merge(List(T(1), T(2)), List(T(2), T(3)))
		// and make sure the set contains all items
		moded should be === List(T(2), T(3))
	}
}