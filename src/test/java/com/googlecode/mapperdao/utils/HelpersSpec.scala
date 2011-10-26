package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 26 Oct 2011
 */
class HelpersSpec extends SpecificationWithJUnit {

	import Helpers._

	case class T(i: Int)

	"added (set)" in {
		val origT1 = T(1)
		val moded = modified(Set(origT1), Set(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded.filter(_.eq(origT1)).size must_== 1
		// and make sure the set contains all items
		moded must_== Set(T(1), T(2), T(3))
	}

	"removed (set)" in {
		modified(Set(T(1)), Set(T(2), T(3))) must_== Set(T(2), T(3))
	}

	"added (list)" in {
		val origT1 = T(1)
		val moded = modified(List(origT1), List(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) must beTheSameAs(origT1)
		// and make sure the set contains all items
		moded must_== List(T(1), T(2), T(3))
	}

	"added different instance but equals, keep 1 only (list)" in {
		val origT1 = T(1)
		val origT1Second = T(1)
		val moded = modified(List(origT1, origT1Second), List(T(1), T(2), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) must beTheSameAs(origT1)
		// and make sure the set contains all items
		moded must_== List(T(1), T(2), T(3))
	}

	"added different instance but equals, keep both (list)" in {
		val origT1 = T(1)
		val origT1Second = T(1)
		val moded = modified(List(origT1, origT1Second), List(T(1), T(2), T(1), T(3)))
		// make sure the set retains origT1 (same instance)
		moded(0) must beTheSameAs(origT1)
		moded(2) must beTheSameAs(origT1Second)
		// and make sure the set contains all items
		moded must_== List(T(1), T(2), T(1), T(3))
	}

	"removed (list)" in {
		val moded = modified(List(T(1), T(2)), List(T(2), T(3)))
		// and make sure the set contains all items
		moded must_== List(T(2), T(3))
	}
}