package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.internal.TraversableSeparation
import com.googlecode.mapperdao.{StringValue, _}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
/**
 * @author kostantinos.kougios
 *
 *         6 Sep 2011
 */
@RunWith(classOf[JUnitRunner])
class TraversableSeparationSuite extends FunSuite
{

	test("intersect") {
		val left = List(xwith(1), xwith(2), xwith(3))
		val right = List(X(0)) ::: left.filterNot(_ == X(2)) ::: List(X(4), X(5))
		val (_, intersect, _) = TraversableSeparation.separate(XEntity, left, right)
		intersect should be(List((X(1), X(1)), (X(3), X(3))))
		intersect.head._1 should be theSameInstanceAs (left.head)
		intersect.tail.head._1 should be theSameInstanceAs (left.tail.tail.head)
	}

	test("added") {
		val left = List(xwith(1), xwith(2))
		val air = TraversableSeparation.separate(XEntity, left, left ::: List(X(3), X(4)))
		air._1 should be(List(X(3), X(4)))
	}

	test("added, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, left ::: List(X(3), X(4)))
		air._1 should be(List(X(3), X(4)))
	}

	test("intersect, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, List(X(0), X(4), X(5)))
		air._2 should be(List())
	}

	test("removed") {
		val left = List(xwith(1), xwith(2), xwith(3))
		val air = TraversableSeparation.separate(XEntity, left, List(X(0)) ::: left.filterNot(x => x == X(2) || x == X(3)) ::: List(X(4), X(5)))
		air._3 should be(List(X(2), X(3)))
	}

	test("removed, left is empty") {
		val left = List()
		val air = TraversableSeparation.separate(XEntity, left, List(X(4), X(5)))
		air._3 should be(List())
	}

	test("right is empty") {
		val left = List(xwith(1), xwith(2))
		val air = TraversableSeparation.separate(XEntity, left, Nil)
		air._1 should be(Nil)
		air._2 should be(Nil)
		air._3 should be(List(X(1), X(2)))
	}

	test("SimpleTypeValue separation, addition") {
		val old = List(swith("kostas"), swith("kougios"))
		val (added, intersect, _) = TraversableSeparation.separate(stringEntity, old, List(StringValue("kostas"), StringValue("kougios"), StringValue("X")))
		added should be(List(StringValue("X")))
		intersect should be(List((StringValue("kostas"), StringValue("kostas")), (StringValue("kougios"), StringValue("kougios"))))
		intersect.head._1 should be theSameInstanceAs (old.head)
		intersect.tail.head._1 should be theSameInstanceAs (old.tail.head)
	}

	case class X(id: Int)

	def xwith(id: Int) = new X(id) with NaturalIntId

	object XEntity extends Entity[Int, NaturalIntId, X]
	{
		val id = key("id") to (_.id)

		def constructor(implicit m: ValuesMap) = new X(id) with Stored
	}

	val stringEntity = StringEntity.oneToMany("", "")

	def swith(s: String) = new StringValue(s) with NoId
}