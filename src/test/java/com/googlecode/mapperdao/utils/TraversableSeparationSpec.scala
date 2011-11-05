package com.googlecode.mapperdao.utils
import org.specs2.mutable.SpecificationWithJUnit
import com.googlecode.mapperdao.StringValue

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

	"added, left is empty" in {
		val left = List()
		val air = TraversableSeparation.separate(left, left ::: List(X(3), X(4)))
		air._1 must_== List(X(3), X(4))
	}

	"intersect" in {
		val left = List(X(1), X(2), X(3))
		val air = TraversableSeparation.separate(left, List(X(0)) ::: left.filterNot(_ == X(2)) ::: List(X(4), X(5)))
		air._2 must_== List(X(1), X(3))
	}

	"intersect, left is empty" in {
		val left = List()
		val air = TraversableSeparation.separate(left, List(X(0), X(4), X(5)))
		air._2 must_== List()
	}

	"removed" in {
		val left = List(X(1), X(2), X(3))
		val air = TraversableSeparation.separate(left, List(X(0)) ::: left.filterNot(x => x == X(2) || x == X(3)) ::: List(X(4), X(5)))
		air._3 must_== List(X(2), X(3))
	}

	"removed, left is empty" in {
		val left = List()
		val air = TraversableSeparation.separate(left, List(X(4), X(5)))
		air._3 must_== List()
	}

	"right is empty" in {
		val left = List(X(1), X(2))
		val air = TraversableSeparation.separate(left, Nil)
		air._1 must_== Nil
		air._2 must_== Nil
		air._3 must_== List(X(1), X(2))
	}

	"SimpleTypeValue separation, addition" in {
		val (added, intersect, removed) = TraversableSeparation.separate(List(StringValue("kostas"), StringValue("kougios")), List(StringValue("kostas"), StringValue("kougios"), StringValue("X")))
		added must_== List(StringValue("X"))
		intersect must_== List(StringValue("kostas"), StringValue("kougios"))

	}

	case class X(id: Int)
}