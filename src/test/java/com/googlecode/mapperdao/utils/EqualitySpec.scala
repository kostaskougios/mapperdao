package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 10 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class EqualitySpec extends SpecificationWithJUnit {

	import Equality._

	"equality of Object, positive" in {
		object X
		isEqual(X, X) must beTrue
	}
	"equality of Object, negative" in {
		object X
		object Y
		isEqual(X, Y) must beFalse
	}

	"equality of Double, positive" in {
		isEqual(7.5d, 7.5d) must beTrue
	}

	"equality of Double, negative" in {
		isEqual(7.5d, 8.5d) must beFalse
	}

	"equality of Float, positive" in {
		isEqual(7.5f, 7.5f) must beTrue
	}

	"equality of Float, negative" in {
		isEqual(7.5f, 8.5f) must beFalse
	}

	"equality of Strings, positive" in {
		isEqual("x1", "x1") must beTrue
	}

	"equality of Strings, negative" in {
		isEqual("x1", "x2") must beFalse
	}

	"equality of Int, positive" in {
		isEqual(7, 7) must beTrue
	}

	"equality of Int, negative" in {
		isEqual(7, 8) must beFalse
	}

	"equality of Long, positive" in {
		isEqual(7l, 7l) must beTrue
	}

	"equality of Long, negative" in {
		isEqual(7l, 8l) must beFalse
	}

	"equality of Char, positive" in {
		isEqual('x', 'x') must beTrue
	}

	"equality of Char, negative" in {
		isEqual('x', 'y') must beFalse
	}
}