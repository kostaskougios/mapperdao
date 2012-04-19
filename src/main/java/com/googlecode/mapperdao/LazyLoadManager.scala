package com.googlecode.mapperdao

import com.googlecode.classgenerator.ClassManager
import com.googlecode.classgenerator.runtime.Args
import com.googlecode.classgenerator.LazyLoadInstanceFactory
import org.objenesis.ObjenesisStd
import com.googlecode.classgenerator.ReflectionManager
import com.googlecode.classgenerator.MethodImplementation

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
private[mapperdao] class LazyLoadManager {

	import LazyLoadManager._

	private val classManager = new ClassManager
	type CacheKey = (Class[_], LazyLoad)

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, LazyLoadInstanceFactory[_, _]]

	private def proxyFor[PC, T](entity: Entity[PC, T], lazyLoad: LazyLoad, vm: ValuesMap): Class[T] = {

		val clz = entity.clz
		val relationships = entity.columns.collect {
			case c: ColumnInfoRelationshipBase[_, _, _, _] => c
		}
		val key = (entity.clz, lazyLoad)

		val factory = classCache.synchronized {
			classCache.get(key) match {
				case null =>
					val methods = relationships.map(ci =>
						ci.getterMethod.getOrElse(throw new IllegalStateException("please define getter method on entity for %s".format(ci.column)))
					).toSet
					val builder = classManager.lazyLoadBuilder(clz, methods).interface[Persisted].get
					val factory = new LazyLoadInstanceFactory[T with Persisted, T with Persisted with MethodImplementation[T]](reflectionManager, builder)
					classCache.put(key, factory)
					factory
				case factory => factory
			}
		}

		val methodToAlias = relationships.map { ci =>
			(ci.getterMethod.get.getName, ci.column.alias)
		}.toMap

		val instantiator = objenesis.getInstantiatorOf(clz)
		instantiator.newInstance
		val f = { args: Args[T, Any] =>
			val alias = methodToAlias(args.methodName)
			vm.valueOf(alias)
		}
	}
}

object LazyLoadManager {
	private val objenesis = new ObjenesisStd
	private val reflectionManager = new ReflectionManager
}