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

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, LazyLoadInstanceFactory[_]]

	def proxyFor[PC, T](constructed: T with PC, entity: Entity[PC, T], lazyLoad: LazyLoad, vm: ValuesMap): T with PC = {

		val clz = constructed.getClass
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
					val factory = new LazyLoadInstanceFactory(reflectionManager, builder)
					classCache.put(key, factory)
					factory
				case factory => factory
			}
		}

		val methodToAlias = relationships.map { ci =>
			(ci.getterMethod.get.getName, ci.column.alias)
		}.toMap

		val instantiator = objenesis.getInstantiatorOf(clz)
		val instance = instantiator.newInstance.asInstanceOf[PC with T with MethodImplementation[T]]
		instance.methodImplementation { args: Args[T, Any] =>
			val alias = methodToAlias(args.methodName)
			vm.valueOf(alias)
		}
		instance
	}
}

object LazyLoadManager {
	private val objenesis = new ObjenesisStd
	private val reflectionManager = new ReflectionManager
}