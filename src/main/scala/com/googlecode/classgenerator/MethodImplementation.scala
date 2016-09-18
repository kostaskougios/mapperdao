package com.googlecode.classgenerator

import com.googlecode.classgenerator.runtime.MImpl.ImplementationFunction

/**
  * provides a way to set the implementation function for the
  * dynamically overriden methods
  *
  * @author kostantinos.kougios
  *
  *         19 Apr 2012
  */
trait MethodImplementation[T]
{
	def methodImplementation(impl: ImplementationFunction[T, Any]): Unit
}