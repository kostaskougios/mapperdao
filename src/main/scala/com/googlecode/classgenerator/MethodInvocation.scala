package com.googlecode.classgenerator

import java.lang.reflect.Method

/**
  * @author kostantinos.kougios
  *
  *         9 Apr 2012
  */
case class MethodInvocation(self: Object, method: Method, proxyMethod: Method, args: Array[Object])
{
	def proceed = proxyMethod.invoke(self, args: _*)
}