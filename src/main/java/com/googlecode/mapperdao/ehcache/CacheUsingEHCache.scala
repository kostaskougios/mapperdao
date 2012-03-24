package com.googlecode.mapperdao.ehcache

import com.googlecode.mapperdao.drivers.Cache
import com.googlecode.mapperdao.CacheOption

import net.sf.ehcache.Element

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2012
 */
class CacheUsingEHCache(val cache: net.sf.ehcache.Cache) extends Cache {

	override def apply[T](key: List[Any], options: CacheOption)(valueCalculator: => T): T = {
		cache.get(key) match {
			case null =>
				val v = valueCalculator
				cache.put(new Element(key, v))
				v
			case v => v.getObjectValue.asInstanceOf[T]
		}
	}
}

trait Locking extends CacheUsingEHCache {
	abstract override def apply[T](key: List[Any], options: CacheOption)(valueCalculator: => T): T = {
		cache.acquireWriteLockOnKey(key)
		try {
			super.apply(key, options)(valueCalculator)
		} finally {
			cache.releaseWriteLockOnKey(key)
		}
	}
}