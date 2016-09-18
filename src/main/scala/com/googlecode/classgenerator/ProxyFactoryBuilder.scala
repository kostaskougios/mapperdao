package com.googlecode.classgenerator

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
class ProxyFactoryBuilder[T](clz: Class[T])
{
	private var interfaces: Array[Class[_]] = Array()

	private var handlers = Map[String, InvokeHandler]()
	private var invokeF: InvokeHandler = null
	private var handled: Option[Handled] = None

	/**
	  * methods that will be proxied
	  */
	def implementMethods(names: Set[String])(handler: InvokeHandler) = {
		val b = Map.newBuilder[String, InvokeHandler] ++= handlers
		names.foreach { n =>
			b += n -> handler
		}
		handlers = b.result
		this
	}

	def implementMultipleMethods(methodsAndImpl: Map[String, InvokeHandler]) = {
		handlers = handlers ++ methodsAndImpl
		this
	}

	/**
	  * provides an InvokeHandler that is called every time a
	  * proxied method is invoked
	  */
	def onInvoke(f: InvokeHandler) = {
		invokeF = f
		this
	}

	/**
	  * alternative way to decide which methods are proxied. (note: this
	  * is combined with previously proxied methods)
	  */
	def decideProxiedMethods(f: Handled) = {
		handled = Some(f)
		this
	}

	/**
	  * provides a list of traits that will be implemented by
	  * the proxy. Please note that traits with implementations
	  * are not supported (it is a scala feature, not a java)
	  */
	def implementTraits(all: Array[Class[_]]) = {
		interfaces ++= all
		this
	}

	/**
	  * the proxy will implement trait X. Example
	  * .implementTrait[MyTrait]
	  */
	def implementTrait[X](implicit m: ClassManifest[X]) = {
		interfaces = interfaces ++ Array(m.erasure)
		this
	}

	/**
	  * the proxy will implement traits X1,X2. Example
	  * .implementTrait[MyTrait1,MyTrait2]
	  */
	def implementTrait[X1, X2](implicit m1: ClassManifest[X1], m2: ClassManifest[X2]) = {
		interfaces = interfaces ++ Array(m1.erasure, m2.erasure)
		this
	}

	/**
	  * the proxy will implement traits X1,X2,X3. Example
	  * .implementTrait[MyTrait1,MyTrait2,MyTrait3]
	  */
	def implementTrait[X1, X2, X3](implicit m1: ClassManifest[X1], m2: ClassManifest[X2], m3: ClassManifest[X3]) = {
		interfaces = interfaces ++ Array(m1.erasure, m2.erasure, m3.erasure)
		this
	}

	/**
	  * builds the ProxyFactory according to previously declared parameters
	  */
	def get: ProxyFactory[T] = new ProxyFactory(
		{ m =>
			handlers.contains(m.getName) || handled.map(_ (m)).getOrElse(false)
		}, interfaces, { (o, m, proceed, args) =>
			val mi = MethodInvocation(o, m, proceed, args)
			handlers.getOrElse(m.getName, invokeF)(mi).asInstanceOf[Object]
		}, clz)
}