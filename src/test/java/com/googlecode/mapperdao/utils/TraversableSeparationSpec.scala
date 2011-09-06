package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
class TraversableSeparationSpec extends SpecificationWithJUnit {

	"added" in {
		val left = List(X(1), X(2))
		val air = TraversableSeparation.separate(left, left ::: List(X(3), X(4)))
		air._1 must_== List(X(3), X(4))
	}

	"intersect" in {
		val left = List(X(1), X(2), X(3))
		val air = TraversableSeparation.separate(left, List(X(0)) ::: left.filterNot(_ == X(2)) ::: List(X(4), X(5)))
		air._2 must_== List(X(1), X(3))
	}

	"removed" in {
		val left = List(X(1), X(2), X(3))
		val air = TraversableSeparation.separate(left, List(X(0)) ::: left.filterNot(x => x == X(2) || x == X(3)) ::: List(X(4), X(5)))
		air._3 must_== List(X(2), X(3))
	}

	case class X(id: Int)
}