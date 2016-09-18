package com.googlecode.classgenerator.model

class TestSubclass
{
	val x = 5
}

class TestSuperclass
{
	val superVal = "x"

	def superVal(i: Int) = "x" + i

	def voidMethod(i: Int): Unit = {
		// nothing
	}
}

trait TTestSuperclass
{
	val superVal: String
}