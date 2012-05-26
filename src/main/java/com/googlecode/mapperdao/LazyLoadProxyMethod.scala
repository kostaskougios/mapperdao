package com.googlecode.mapperdao

import com.googlecode.classgenerator.runtime.Args
import java.lang.reflect.Method
import LazyLoadManager._

/**
 * @author kostantinos.kougios
 *
 * 26 May 2012
 */
class LazyLoadProxyMethod[T](
		persistedMethodNamesToMethod:Map[String,Method]
		) extends (Args[T with Persisted, Any]=>Any) {
	
			// provide an implementation for the proxied methods
private		var alreadyCalled = Set.empty[String]

	def apply(args: Args[T with Persisted, Any] ) = {
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
					reflectionManager.callMethod(method, persisted, args.args)
				} else if (isSetter(methodName)) {
					// setter
					alreadyCalled += getterFromSetter(args.methodName)
					args.callSuper
				} else if (methodName == "freeLazyLoadMemoryData") {
					toLazyLoad.clear()
					methodToCI.map(_._1).foreach {
						alreadyCalled += _
					}
				} else {
					// getter
					if (!alreadyCalled(args.methodName)) {
						alreadyCalled += args.methodName

						val ci = methodToCI(args.methodName)
						val gm = ci.getterMethod.get
						val alias = ci.column.alias

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
									val con = converters.getOrElse(returnType, gm.converter.getOrElse(throw new IllegalStateException("type %s not supported for getter. Please define a converter function".format(returnType))))
									con(v)
								}
							case _ => v
						}
						val t = args.self
						reflectionManager.set(gm.fieldName, t, r)
						if (t.mapperDaoValuesMap != null)
							t.mapperDaoValuesMap(ci) = r
						r
					} else {
						args.callSuper
					}
				}
			}
		}

	}

}