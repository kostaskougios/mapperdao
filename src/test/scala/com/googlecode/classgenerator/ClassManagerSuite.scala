package com.googlecode.classgenerator

import com.googlecode.classgenerator.model.{TTestSuperclass, TestSubclass, TestSuperclass}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
@RunWith(classOf[JUnitRunner])
class ClassManagerSuite extends FunSuite with ShouldMatchers
{

	val classManager = new ClassManager

	test("override 1-arg method of super class that returns void and call it") {
		val clz = classManager.buildNewClass("test.Proceed4")
			.superClass[TestSuperclass]
			.method("voidMethod")
			.argTypes[Int]
			.enableSuperMethodInvocation
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			args.callSuper
		}
		i.superVal(8)
	}

	test("override noarg method of super class and call it") {
		val clz = classManager.buildNewClass("test.Proceed1")
			.superClass[TestSuperclass]
			.method("superVal")
			.returnType[String]
			.enableSuperMethodInvocation
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			val s = args.callSuper
			s + "-mod"
		}
		i.superVal should be("x-mod")
	}

	test("override 1-arg method of super class and call it") {
		val clz = classManager.buildNewClass("test.Proceed2")
			.superClass[TestSuperclass]
			.method("superVal")
			.returnType[String]
			.argTypes[Int]
			.enableSuperMethodInvocation
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			val s = args.callSuper
			s + "-mod"
		}
		i.superVal(8) should be("x8-mod")
	}

	test("override 1-arg method of super class and call it with different arguments") {
		val clz = classManager.buildNewClass("test.Proceed3")
			.superClass[TestSuperclass]
			.method("superVal")
			.returnType[String]
			.argTypes[Int]
			.enableSuperMethodInvocation
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			val s = args.callSuper(Array(10))
			s + "-mod"
		}
		i.superVal(8) should be("x10-mod")
	}

	test("subclass") {
		trait X
		{
			def x: Int
		}
		val clz = classManager.buildNewClass("test.SubclassOf")
			.superClass[TestSuperclass]
			.interface[X]
			.method("x")
			.returnType[Int]
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			5
		}
		i.x should be(5)
		i.superVal should be("x")
	}

	test("template with existing class") {
		trait X
		{
			def x: Int
		}
		val clz = classManager.buildUsing[TestSuperclass]
			.name("test.CWithX")
			.interface[X]
			.interface[TTestSuperclass]
			.method("x")
			.returnType[Int]
			.implementation
			.get
		val i = clz.newInstance
		i.methodImplementation { args =>
			5
		}
		i.x should be(5)
		i.superVal should be("x")
	}

	test("add method, 2 arguments") {
		trait X
		{
			def x(p1: Int, p2: String): String
		}
		val clz = classManager.buildNewClass("test.InterfaceWith2Args")
			.interface[X]
			.method("x")
			.returnType[String]
			.argTypes[Int, String]
			.implementation
			.get

		val i = clz.newInstance
		i.methodImplementation { args =>
			val i = args[Int](0)
			args[Int](0).toString + args[String](1)
		}
		i.x(1, "x") should be("1x")
	}

	test("add method, int") {
		trait X
		{
			def x(p1: Int): Int
		}
		val clz = classManager.buildNewClass("test.Interface3")
			.interface[X]
			.method("x")
			.returnType[Int]
			.argTypes[Int]
			.implementation
			.get

		val i = clz.newInstance
		i.methodImplementation { args =>
			val i = args[Int](0)
			args.methodName should be("x")
			i + 1
		}
		i.x(1) should be(2)
	}

	test("add method, float") {
		trait X
		{
			def x(p1: Float): Float
		}
		val clz = classManager.buildNewClass("test.Interface4")
			.interface[X]
			.method("x")
			.returnType[Float]
			.argTypes[Float]
			.implementation
			.get

		val i = clz.newInstance
		i.methodImplementation { args =>
			args.methodName should be("x")
			val i = args[Float](0)
			i + 1.5f
		}
		i.x(1) should be(2.5)
	}

	test("add method, String") {
		trait X
		{
			def x(p1: String): String
		}
		val clz = classManager.buildNewClass("test.Interface5")
			.interface[X]
			.method("x")
			.returnType[String]
			.argTypes[String]
			.implementation
			.get

		val i = clz.newInstance
		i.methodImplementation { args =>
			args.methodName should be("x")
			val i = args[String](0)
			i + "x"
		}
		i.x("a") should be("ax")
	}

	test("add method, returns Unit") {
		trait X
		{
			def x(p1: String): Unit
		}
		var c = 0
		val clz = classManager.buildNewClass("test.Interface6")
			.interface[X]
			.method("x")
			.returnType[Unit]
			.argTypes[String]
			.implementation
			.get

		val i = clz.newInstance
		i.methodImplementation { args =>
			args.methodName should be("x")
			c += 1
		}
		i.x("a")
		c should be(1)
	}

	test("add method, low level") {
		trait X
		{
			def x: Int
		}
		val clz = classManager.buildNewClass("test.Interface2")
			.interface[X]
			.methodWithSrc("public int x() { return 10; }")
			.get

		val i = clz.newInstance
		i.x should be(10)
	}

	test("interface") {
		trait X
		val clz = classManager.buildNewClass("test.Interface1").interface[X].get
		classOf[X].isAssignableFrom(clz) should be(true)
	}

	test("superclass") {
		val clz = classManager.buildUsing[TestSubclass]
			.name("TestSubclass$GEN1")
			.superClass[TestSuperclass].get

		classOf[TestSuperclass].isAssignableFrom(clz) should be(true)
		val t: TestSuperclass = clz.newInstance.asInstanceOf[TestSuperclass]
		t.superVal should be("x")
	}

	test("superclass is original class") {
		val clz = classManager.buildUsing[TestSubclass]
			.name("TestSubclass$GEN2")
			.superClass[TestSubclass].get

		classOf[TestSubclass].isAssignableFrom(clz) should be(true)
		val t = clz.newInstance
		t.x should be(5)
	}

	test("add field, low level") {
		val clz = classManager.buildNewClass("test.NewClass1").field("public int x=1;").get
		clz.getField("x") // will throw NoSuchFieldException if not present
	}

	test("add static field, low level") {
		val clz = classManager.buildNewClass("test.NewClass2").field("public static int x=1;").get
		clz.getField("x") // will throw NoSuchFieldException if not present
	}
}