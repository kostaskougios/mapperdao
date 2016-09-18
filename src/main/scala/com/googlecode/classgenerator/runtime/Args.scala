package com.googlecode.classgenerator.runtime

import java.lang.reflect.Method

/**
  * @author kostantinos.kougios
  *
  *         12 Apr 2012
  */
case class Args[T, ReturnT](method: Method, superMethod: Method, self: T, args: Array[Any])
{
	def apply[T](i: Int) = args(i).asInstanceOf[T]

	def callSuper: ReturnT = callSuper(args)

	def callSuper(newArgs: Array[Any]): ReturnT = {
		if (superMethod == null) throw new IllegalStateException("superMethod could not be found for " + method)
		superMethod.invoke(self, newArgs.asInstanceOf[Array[Object]]: _*).asInstanceOf[ReturnT]
	}

	def methodName = method.getName
}