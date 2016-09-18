package com.googlecode.classgenerator

import java.lang.reflect.{Constructor, Method}
import javassist.util.proxy.{MethodFilter, MethodHandler, Proxy}

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
class ProxyFactory[T] protected[classgenerator](
	handled: Handled,
	interfaces: Array[Class[_]],
	invokeF: (Object, Method, Method, Array[Object]) => Object,
	clz: Class[T]
)
{

	private val pf = new javassist.util.proxy.ProxyFactory
	if (!interfaces.isEmpty) pf.setInterfaces(interfaces)
	pf.setSuperclass(clz)
	pf.setFilter(new MethodFilter
	{
		def isHandled(m: Method) = handled(m)
	})

	val proxyClass = pf.createClass.asInstanceOf[Class[T]]

	def constructor(args: Class[_]*) =
		proxyClass.getConstructor(args: _*)

	def newInstance(constructor: Constructor[T], params: Array[Any]) = {
		val n = constructor.newInstance(params.asInstanceOf[Array[Object]]: _*)
		init(n)
		n
	}

	def newInstance = {
		val n = proxyClass.newInstance
		init(n)
		n
	}

	private val methodHandler = new MethodHandler
	{
		override def invoke(self: Object, m: Method, proceed: Method, args: Array[Object]): Object =
			invokeF(self, m, proceed, args).asInstanceOf[Object]
	}

	private def init(t: T) {
		val proxy = t.asInstanceOf[Proxy]
		proxy.setHandler(methodHandler)
	}
}