package com.googlecode.classgenerator.model

/**
  * @author kostantinos.kougios
  *
  *         15 Apr 2012
  */

class Super(val l: List[Int])
{
	var x = 5
	val notLazy = 8

	def lAgain = l

	def xFun = (i: Int) => x + i
}

case class CaseSuper(val l: List[Int], var x: Int)
{
	def lAgain = l

	def xFun = (i: Int) => x + i
}

trait Lazy
{
	def l: List[Int]

	def x: Int
}
