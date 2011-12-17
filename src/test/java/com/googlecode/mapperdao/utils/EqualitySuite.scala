package com.googlecode.mapperdao.utils
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 10 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class EqualitySuite extends FunSuite with ShouldMatchers {

	import Equality._

	test("equality of Object, positive") {
		object X
		isEqual(X, X) should be(true)
	}
	test("equality of Object, negative") {
		object X
		object Y
		isEqual(X, Y) should be(false)
	}

	test("equality of Double, positive") {
		isEqual(7.5d, 7.5d) should be(true)
	}

	test("equality of Double, negative") {
		isEqual(7.5d, 8.5d) should be(false)
	}

	test("equality of Float, positive") {
		isEqual(7.5f, 7.5f) should be(true)
	}

	test("equality of Float, negative") {
		isEqual(7.5f, 8.5f) should be(false)
	}

	test("equality of Strings, positive") {
		isEqual("x1", "x1") should be(true)
	}

	test("equality of Strings, negative") {
		isEqual("x1", "x2") should be(false)
	}

	test("equality of Int, positive") {
		isEqual(7, 7) should be(true)
	}

	test("equality of Int, negative") {
		isEqual(7, 8) should be(false)
	}

	test("equality of Long, positive") {
		isEqual(7l, 7l) should be(true)
	}

	test("equality of Long, negative") {
		isEqual(7l, 8l) should be(false)
	}

	test("equality of Char, positive") {
		isEqual('x', 'x') should be(true)
	}

	test("equality of Char, negative") {
		isEqual('x', 'y') should be(false)
	}
}