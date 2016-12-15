package com.googlecode.classgenerator

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

/**
  * @author kostantinos.kougios
  *
  *         4 Jun 2012
  */
@RunWith(classOf[JUnitRunner])
class DeepFieldVisitorSuite extends FunSuite
{
	val rm = new ReflectionManager
	val dfv = new DeepFieldVisitor(rm)
	test("deep visit two with null") {
		val result = dfv.visitTwo(
			Person("kostas", Dog("greta", 5), 20),
			Person("tina", null, 18)
		) { (o1, o2, field, v1, v2) =>
			(field.getName, v1, v2)
		}
		result.toSet should be(Set(
			("name", "kostas", "tina"),
			("pet", Dog("greta", 5), null),
			("age", 20, 18)
		))
	}

	test("deep visit two") {
		val result = dfv.visitTwo(
			Person("kostas", Dog("greta", 5), 20),
			Person("tina", Dog("koutch", 7), 18)
		) { (o1, o2, field, v1, v2) =>
			(field.getName, v1, v2)
		}
		result.toSet should be(Set(
			("name", "kostas", "tina"),
			("pet", Dog("greta", 5), Dog("koutch", 7)),
			("age", 20, 18),
			("name", "greta", "koutch"),
			("age", 5, 7)
		))
	}

	test("deep visit") {
		val result = dfv.visit(Person("kostas", Dog("greta", 5), 20)) { (o, field, v) =>
			(field.getName, v)
		}
		result.toSet should be(Set(
			("name", "kostas"),
			("pet", Dog("greta", 5)),
			("age", 20),
			("name", "greta"),
			("age", 5)
		))
	}

	test("deep visit, nulls") {
		val result = dfv.visit(Person("kostas", null, 20)) { (o, field, v) =>
			(field.getName, v)
		}
		result.toSet should be(Set(
			("name", "kostas"),
			("pet", null),
			("age", 20)
		))
	}

	case class Person(name: String, pet: Dog, age: Int)

	case class Dog(name: String, age: Int)

}