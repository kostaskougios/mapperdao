package com.googlecode.classgenerator

import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicInteger
import javassist.ClassPool

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
class ClassManager(
	pool: ClassPool = {
		val cp = new ClassPool(null)
		cp.appendSystemPath()
		cp
	}, reflectionManager: ReflectionManager = new ReflectionManager
)
{

	import ClassManager._

	importPackage("com.googlecode.classgenerator.runtime")

	def importPackage(pcg: String) = pool.importPackage(pcg)

	def buildNewClass(clzName: String): ClassBuilder[AnyRef] = buildNewTypedClass[AnyRef](clzName)

	def buildNewTypedClass[T](clzName: String): ClassBuilder[T] = {
		val ctClass = pool.makeClass(clzName)
		new ClassBuilder(pool, ctClass, reflectionManager)
	}

	def buildUsing[T](clzName: String): ClassBuilder[AnyRef] = {
		val ctClz = pool.get(clzName)
		new ClassBuilder(pool, ctClz, reflectionManager)
	}

	def buildNewSubclass[T](clz: Class[T]): ClassBuilder[T] = {
		val cnt = subclassLoadCounter.incrementAndGet
		buildNewTypedClass[T](clz.getName + "_$" + cnt)
			.superClass[T](clz)
	}

	/**
	  * builds using T as template. Note that the returned ClassBuilder is not of type T
	  * and the returned class is not instanceof T. T is just a template to build a new
	  * class.
	  */
	def buildUsing[T](implicit m: ClassManifest[T]): ClassBuilder[AnyRef] = buildUsing(m.erasure.getName)

	/**
	  * gets a proxy builder for the selected type. Example:
	  *
	  * <code>
	  * import com.googlecode.scalassist._
	  * val proxy = classManager.proxy[TestProxy]
	  * .handledMethodNames(Set(getter("anInt"), setter("anInt")))
	  * .onInvoke { (o, m, proceed, args) =>
	  * 			proceed.invoke(o, args: _*)
	  * }.get
	  * </code>
	  *
	  * @return ProxyFactoryBuilder which can be further used to specify
	  *         the properties of the ProxyFactory
	  */
	def proxy[T](implicit m: ClassManifest[T]): ProxyFactoryBuilder[T] =
	new ProxyFactoryBuilder(m.erasure.asInstanceOf[Class[T]])

	def lazyLoadBuilder[T](clz: Class[T], template: Class[_]): ClassBuilder[T with MethodImplementation[T]] =
		buildNewSubclass(clz)
			.implementFromTrait(template, true)
			.implementSettersIfExistFromTrait(clz, template)

	def lazyLoadBuilder[T](clz: Class[T], methods: Set[Method]): ClassBuilder[T with MethodImplementation[T]] = {
		buildNewSubclass(clz)
			.overrideMethods(methods)
			.overrideSettersIfExist(clz, methods)
	}

	def lazyLoad[T](clz: Class[T], template: Class[_]) =
		new LazyLoadInstanceFactory(reflectionManager, lazyLoadBuilder(clz, template).get)

	def lazyLoad[T](clz: Class[T], methods: Set[Method]) =
		new LazyLoadInstanceFactory(reflectionManager, lazyLoadBuilder(clz, methods).get)
}

object ClassManager
{
	private val subclassLoadCounter = new AtomicInteger
}