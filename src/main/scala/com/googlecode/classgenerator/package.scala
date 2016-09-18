package com.googlecode

import java.lang.reflect.Method

/**
  * @author kostantinos.kougios
  *
  *         9 Apr 2012
  */
package object classgenerator
{
	type Handled = Method => Boolean
	type InvokeHandler = MethodInvocation => Any

	def getter(name: String) = name

	def setter(name: String) = name + "_$eq"

	def isSetter(name: String) = name.endsWith("_$eq")

	def getterFromSetter(name: String) = name.substring(0, name.length - 4) // remove _$eq
}