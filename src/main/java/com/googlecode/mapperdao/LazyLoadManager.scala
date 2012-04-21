package com.googlecode.mapperdao

import com.googlecode.classgenerator.ClassManager
import com.googlecode.classgenerator.runtime.Args
import com.googlecode.classgenerator.LazyLoadInstanceFactory
import org.objenesis.ObjenesisStd
import com.googlecode.classgenerator.ReflectionManager
import com.googlecode.classgenerator.MethodImplementation
import java.lang.reflect.Method

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
private[mapperdao] class LazyLoadManager {

	import LazyLoadManager._

	private val classManager = new ClassManager
	type CacheKey = (Class[_], LazyLoad)

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, Class[_]]

	def proxyFor[PC, T](constructed: T with PC, entity: Entity[PC, T], lazyLoad: LazyLoad, vm: ValuesMap): T with PC = {

		val clz = entity.clz
		val relationships = entity.columns.collect {
			case c: ColumnInfoRelationshipBase[_, _, _, _] => c
		}
		val key = (clz, lazyLoad)

		val proxyClz = classCache.synchronized {
			classCache.get(key).getOrElse {
				val methods = relationships.map(ci =>
					ci.getterMethod.getOrElse(throw new IllegalStateException("please define getter method on entity for %s".format(ci.column)))
				).toSet
				if (methods.isEmpty) throw new IllegalStateException("can't lazy load class that doesn't declare any getters for relationships. Entity: %s".format(clz))
				val proxyClz = createProxyClz(clz, methods);
				classCache.put(key, proxyClz)
				proxyClz
			}
		}

		val methodToAlias = relationships.map { ci =>
			(ci.getterMethod.get.getName, ci.column.alias)
		}.toMap

		val instantiator = objenesis.getInstantiatorOf(proxyClz)
		val instance = instantiator.newInstance.asInstanceOf[PC with T with MethodImplementation[T]]
		instance.methodImplementation { args: Args[T, Any] =>
			val alias = methodToAlias(args.methodName)
			vm.valueOf(alias)
		}
		instance
	}

	private def createProxyClz(clz: Class[_], methods: Set[Method]) = {
		classManager.buildNewSubclass(clz)
			.interface[Persisted]
			.overrideMethods(clz, methods)
			.get
	}
}

object LazyLoadManager {
	private val objenesis = new ObjenesisStd
	private val reflectionManager = new ReflectionManager
}