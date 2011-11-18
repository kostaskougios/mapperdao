package com.googlecode.mapperdao
import org.specs2.mutable.SpecificationWithJUnit
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
@RunWith(classOf[JUnitRunner])
class EntityMapSpec extends SpecificationWithJUnit {

	case class E1(n: String)
	case class E2(n: String)

	"put and get" in {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		m.put(classOf[E1], List(6), E1("2"))
		m.put(classOf[E2], List(5, 6), E2("x"))
		m.put(classOf[E2], List(5, 7), E2("y"))

		m.get[E1](classOf[E1], List(5)).get must_== E1("1")
		m.get[E1](classOf[E1], List(6)).get must_== E1("2")

		m.get[E2](classOf[E2], List(5, 6)).get must_== E2("x")
		m.get[E2](classOf[E2], List(5, 7)).get must_== E2("y")
	}

	"get from empty" in {
		val m = new EntityMap
		m.get[E2](classOf[E2], List(5, 6)) must beNone
	}

	"re-put throws exception" in {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		m.put(classOf[E1], List(5), E1("1")) must throwA[IllegalStateException]
	}

	"reput" in {
		val m = new EntityMap
		m.put(classOf[E1], List(5), E1("1"))
		m.reput(classOf[E1], List(5), E1("2"))
		m.get[E1](classOf[E1], List(5)).get must_== E1("2")
	}
}