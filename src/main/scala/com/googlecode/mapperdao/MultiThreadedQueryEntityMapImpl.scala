package com.googlecode.mapperdao

import java.util.concurrent.ConcurrentHashMap

/**
 * @author kostantinos.kougios
 *
 *         6 May 2012
 */
private[mapperdao] class MultiThreadedQueryEntityMapImpl(m: ConcurrentHashMap[List[Any], Option[_]]) extends EntityMap
{
	override def get[T](clz: Class[_], ids: List[Any])(f: => Option[T]): Option[T] = {
		// try to get the value from m just in case it was already loaded
		// by a different thread. If not found, get it as usual and cache
		// it in m

		val k = key(clz, ids)
		m.get(k) match {
			case null =>
				val vo = super.get(clz, ids)(f)
				vo match {
					case Some(v) =>
						v match {
							case p: Persisted if (!p.mapperDaoValuesMap.mock) =>
								// we can store non-mock into the global cache
								m.put(k, vo)
							case _ =>
						}
					case _ =>
				}
				vo
			case vo => vo.asInstanceOf[Option[T]]
		}
	}
}