package com.googlecode.mapperdao

import com.googlecode.classgenerator.ClassManager
import com.googlecode.classgenerator.runtime.Args

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
private[mapperdao] class LazyLoadManager {
	private val classManager = new ClassManager
	type CacheKey = (Class[_], LazyLoad)

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, Class[_]]

	private def proxyFor[PC, T](entity: Entity[PC, T], lazyLoad: LazyLoad, vm: ValuesMap): Class[T] = {
		val clz = entity.clz
		val relationships = entity.columns.collect {
			case c: ColumnInfoRelationshipBase[_, _, _, _] => c
		}
		val methods = relationships.map(ci =>
			ci.getterMethod.getOrElse(throw new IllegalStateException("please define getter method for %s".format(ci.column)))
		).toSet
		val methodToAlias = relationships.map { ci =>
			(ci.getterMethod.get.getName, ci.column.alias)
		}.toMap
		val f = { args: Args[T, Any] =>
			val alias = methodToAlias(args.methodName)
			vm.valueOf(alias)
		}
		val key = (entity.clz, lazyLoad)
		classCache.synchronized {
			classCache.get(key) match {
				case None =>
					val c = classManager.lazyLoad(clz, methods)(f)
					classCache.put(key, c)
					c
				case Some(c) => c.asInstanceOf[Class[T]]
			}
		}
	}

}