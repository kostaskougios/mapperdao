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
		def calculate = {
			val v = valueCalculator
			cache.put(new Element(key, v))
			v
		}
		cache.get(key) match {
			case null =>
				calculate
			case v =>
				val dt = System.currentTimeMillis - v.getCreationTime
				if (dt <= options.expireInMillis)
					v.getObjectValue.asInstanceOf[T]
				else calculate
		}
	}

	override def put[T](key: List[Any], t: T) {
		cache.put(new Element(key, t))
	}

	override def flush(key: List[Any]) {
		cache.remove(key)
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