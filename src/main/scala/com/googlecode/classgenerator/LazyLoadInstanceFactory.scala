package com.googlecode.classgenerator

import java.lang.reflect.Constructor

import com.googlecode.classgenerator.runtime.Args
import com.googlecode.classgenerator.runtime.MImpl.ImplementationFunction

/**
  * @author kostantinos.kougios
  *
  *         19 Apr 2012
  */
class LazyLoadInstanceFactory[T](
	reflectionManager: ReflectionManager,
	clz: Class[_ <: T with MethodImplementation[T]]
)
{

	type ConstructorT = Constructor[T with MethodImplementation[T]]

	def apply(
		c: ConstructorT,
		args: Array[Any]
	)(impl: ImplementationFunction[T, Any]): T = {
		val i = c.newInstance(args.asInstanceOf[Array[Object]]: _*)
		i.methodImplementation(lazyLoadMethodImplementation(impl))
		i
	}

	def apply(idx: Int, args: Array[Any])(impl: ImplementationFunction[T, Any]): T = {
		val c = reflectionManager.constructor(clz, idx).asInstanceOf[ConstructorT]
		apply(c, args)(impl)
	}

	private def lazyLoadMethodImplementation(impl: ImplementationFunction[T, Any]) = {
		val alreadyCalled = new scala.collection.mutable.HashSet[String]

		var f = { args: Args[T, Any] =>
			if (args.methodName.endsWith("_$eq")) {
				// setter
				alreadyCalled += getterFromSetter(args.methodName)
				args.callSuper
			} else {
				// getter
				if (!alreadyCalled(args.methodName)) {
					alreadyCalled += args.methodName
					val r = impl(args)
					reflectionManager.set(args.methodName, args.self, r)
					r
				} else {
					args.callSuper
				}
			}
		}
		f
	}
}