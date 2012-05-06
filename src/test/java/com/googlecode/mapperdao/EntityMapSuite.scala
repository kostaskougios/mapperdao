package com.googlecode.mapperdao
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class EntityMapSuite extends FunSuite with ShouldMatchers {

	case class E1(n: String)
	case class E2(n: String)

	test("put and get") {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		m.put(classOf[E1], List(6), E1("2"))
		m.put(classOf[E2], List(5, 6), E2("x"))
		m.put(classOf[E2], List(5, 7), E2("y"))

		m.get[E1](classOf[E1], List(5)) { None }.get should be === E1("1")
		m.get[E1](classOf[E1], List(6)) { None }.get should be === E1("2")

		m.get[E2](classOf[E2], List(5, 6)) { None }.get should be === E2("x")
		m.get[E2](classOf[E2], List(5, 7)) { None }.get should be === E2("y")
	}

	test("get from empty") {
		val m = new EntityMap
		m.get[E2](classOf[E2], List(5, 6)) { None } should be(None)
	}

	test("re-put throws exception") {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		evaluating { m.put(classOf[E1], List(5), E1("1")) } should produce[IllegalStateException]
	}

	test("reput") {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		m.reput(classOf[E1], List(5), E1("2"))
		m.get[E1](classOf[E1], List(5)) { None }.get should be === E1("2")
	}
}