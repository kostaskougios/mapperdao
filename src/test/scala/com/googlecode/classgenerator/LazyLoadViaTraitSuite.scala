package com.googlecode.classgenerator

import com.googlecode.classgenerator.model.{CaseSuper, Lazy, Super}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
  * @author kostantinos.kougios
  *
  *         15 Apr 2012
  */
@RunWith(classOf[JUnitRunner])
class LazyLoadViaTraitSuite extends FunSuite with ShouldMatchers
{

	val classManager = new ClassManager
	val reflectionManager = new ReflectionManager

	test("lazy load var, lazy loader not called if setter was called") {
		var called = 0
		val factory = classManager.lazyLoad(classOf[Super], classOf[Lazy])
		val i = factory(0, Array[Any](null)) { args =>
			called += 1
			args.methodName match {
				case "x" => 11
			}
		}
		i.x = 12
		i.x should be(12)
		called should be(0)
		i.x = 13
		i.x should be(13)
		called should be(0)
	}

	test("lazy load val") {
		var called = 0
		val factory = classManager.lazyLoad(classOf[Super], classOf[Lazy])
		val i = factory(0, Array[Any](null)) { args =>
			called += 1
			args.methodName match {
				case "l" => List(5)
			}
		}
		i.l should be(List(5))
		called should be(1)
		i.notLazy should be(8)
		called should be(1)
		i.l should be(List(5))
		called should be(1)
		i.lAgain should be(List(5))
		called should be(1)
	}

	test("lazy load var") {
		var called = 0
		val factory = classManager.lazyLoad(classOf[Super], classOf[Lazy])
		val i = factory(0, Array[Any](null)) { args =>
			called += 1
			args.methodName match {
				case "x" => 11
			}
		}
		i.x should be(11)
		called should be(1)
		i.notLazy should be(8)
		called should be(1)
		i.x should be(11)
		called should be(1)
	}

	test("lazy load case class val") {
		var called = 0
		val factory = classManager.lazyLoad(classOf[CaseSuper], classOf[Lazy])
		val i = factory(0, Array(null, 0)) { args =>
			called += 1
			args.methodName match {
				case "l" => List(5)
			}
		}
		i.l should be(List(5))
		called should be(1)
		i.l should be(List(5))
		called should be(1)
		i.lAgain should be(List(5))
		called should be(1)
	}

	test("lazy load case class var") {
		var called = 0
		val factory = classManager.lazyLoad(classOf[CaseSuper], classOf[Lazy])
		val i = factory(0, Array(null, 0)) { args =>
			called += 1
			args.methodName match {
				case "x" => 11
			}
		}
		i.x should be(11)
		called should be(1)
		i.x should be(11)
		called should be(1)
	}
}