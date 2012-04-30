package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.StringValue
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.googlecode.mapperdao.SimpleEntity
import com.googlecode.mapperdao.StringEntity
import com.googlecode.mapperdao.Persisted

/**
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class TraversableSeparationSuite extends FunSuite with ShouldMatchers {

	test("added") {
		val left = List(X(1), X(2))
		val air = TraversableSeparation.separate(XEntity, left, left ::: List(X(3), X(4)))
		air._1 should be === List(X(3), X(4))
	}

	test("added, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, left ::: List(X(3), X(4)))
		air._1 should be === List(X(3), X(4))
	}

	test("intersect") {
		val left = List(X(1), X(2), X(3))
		val (added, intersect, removed) = TraversableSeparation.separate(XEntity, left, List(X(0)) ::: left.filterNot(_ == X(2)) ::: List(X(4), X(5)))
		intersect should be === List(X(1), X(3))
		intersect.head should be theSameInstanceAs (left.head)
		intersect.tail.head should be theSameInstanceAs (left.tail.tail.head)
	}

	test("intersect, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, List(X(0), X(4), X(5)))
		air._2 should be === List()
	}

	test("removed") {
		val left = List(X(1), X(2), X(3))
		val air = TraversableSeparation.separate(XEntity, left, List(X(0)) ::: left.filterNot(x => x == X(2) || x == X(3)) ::: List(X(4), X(5)))
		air._3 should be === List(X(2), X(3))
	}

	test("removed, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, List(X(4), X(5)))
		air._3 should be === List()
	}

	test("right is empty") {
		val left = List(X(1), X(2))
		val air = TraversableSeparation.separate(XEntity, left, Nil)
		air._1 should be === Nil
		air._2 should be === Nil
		air._3 should be === List(X(1), X(2))
	}

	test("SimpleTypeValue separation, addition") {
		val old = List(StringValue("kostas"), StringValue("kougios"))
		val (added, intersect, removed) = TraversableSeparation.separate(stringEntity, old, List(StringValue("kostas"), StringValue("kougios"), StringValue("X")))
		added should be === List(StringValue("X"))
		intersect should be === List(StringValue("kostas"), StringValue("kougios"))
		intersect.head should be theSameInstanceAs (old.head)
		intersect.tail.head should be theSameInstanceAs (old.tail.head)
	}

	case class X(id: Int)
	object XEntity extends SimpleEntity[X] {
		def constructor(implicit m) = new X(1) with Persisted
	}

	val stringEntity = StringEntity.oneToMany("", "", "")
}