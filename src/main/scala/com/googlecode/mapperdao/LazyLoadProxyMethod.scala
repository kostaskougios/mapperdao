package com.googlecode.mapperdao

import com.googlecode.classgenerator.runtime.Args
import LazyLoadManager._
import com.googlecode.classgenerator._

/**
 * implements lazy loaded methods
 *
 * @author kostantinos.kougios
 *
 *         26 May 2012
 */
protected class LazyLoadProxyMethod[T](
	private var toLazyLoad: scala.collection.mutable.Map[ColumnInfoRelationshipBase[T, Any, Any, Any], () => Any],
	private var methodToCI: Map[String, ColumnInfoRelationshipBase[T, Any, Any, Any]]
	)
	extends (Args[T with Persisted, Any] => Any) with Persisted
{

	import LazyLoadProxyMethod._

	// provide an implementation for the proxied methods
	private val alreadyCalled = scala.collection.mutable.Set.empty[String]

	def apply(args: Args[T with Persisted, Any]) = {
		val methodName = args.methodName
		val persistedMethodOption = persistedMethodNamesToMethod.get(methodName)
		// this getter might be called by multiple threads
		// on the same time. We need to ensure that we aquire a lock
		// (not on this though as it might be locked by client code)
		// and that each op is executed only once.
		alreadyCalled.synchronized {
			if (persistedMethodOption.isDefined) {
				// method from Persisted trait
				val method = persistedMethodOption.get
				reflectionManager.callMethod(method, this, args.args)
			} else if (isSetter(methodName)) {
				// setter
				alreadyCalled += getterFromSetter(args.methodName)
				args.callSuper
			} else if (methodName == "freeLazyLoadMemoryData") {
				if (toLazyLoad != null) {
					toLazyLoad.clear()
					methodToCI.map(_._1).foreach {
						alreadyCalled += _
					}
				}
			} else {
				// getter
				if (!alreadyCalled(args.methodName)) {
					alreadyCalled += args.methodName

					val ci = methodToCI(args.methodName)
					val gm = ci.getterMethod.get

					// we need to remove the values
					// to free memory usage
					val v = toLazyLoad(ci)()
					toLazyLoad -= ci
					val r = v match {
						case _: Traversable[_] =>
							val returnType = args.method.getReturnType
							if (returnType.isArray) {
								val ct = returnType.getComponentType
								val am = ClassManifest.fromClass(ct.asInstanceOf[Class[Any]])
								v.asInstanceOf[List[_]].toArray(am)
							} else {
								val con = converters.getOrElse(
									returnType,
									gm.converter.getOrElse(throw new IllegalStateException("type %s not supported for getter. Please define a converter function: getter(method,field,conversion_function). The conversion function should map the value to a Set or List or Traversable or IndexedSeq".format(returnType)))
								)
								con(v)
							}
						case _ => v
					}
					val t = args.self
					reflectionManager.set(gm.fieldName, t, r)

					// free up some memory
					if (t.mapperDaoValuesMap != null)
						t.mapperDaoValuesMap(ci) = v

					if (toLazyLoad != null && toLazyLoad.isEmpty) {
						toLazyLoad = null
						methodToCI = null
					}
					r
				} else {
					args.callSuper
				}
			}
		}
	}
}

object LazyLoadProxyMethod
{
	// convert collections returned by mapperdao to actual collections
	// required by entities
	private val converters = Map[Class[_], Any => Any](
		classOf[Set[_]] -> {
			_.asInstanceOf[List[_]].toSet
		},
		classOf[List[_]] -> {
			_.asInstanceOf[List[_]]
		},
		classOf[IndexedSeq[_]] -> {
			_.asInstanceOf[List[_]].toIndexedSeq
		},
		classOf[Traversable[_]] -> {
			_.asInstanceOf[List[_]]
		}
	)
}