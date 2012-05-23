package com.googlecode.mapperdao.utils
import scala.collection.mutable.ListMap

/**
 * @author kostantinos.kougios
 *
 * 23 Sep 2011
 */
class LowerCaseMutableMap[V] {
	private val m = new ListMap[String, V]
	def this(map: scala.collection.Map[String, V]) =
		{
			this()
			map.foreach { case (k, v) => m(k.toLowerCase) = v }
		}

	def get(key: String): Option[V] = this.synchronized {
		m.get(key.toLowerCase)
	}
	def update(key: String, v: V) = this.synchronized {
		m(key.toLowerCase) = v
	}

	def apply(key: String): V = this.synchronized {
		m(key.toLowerCase)
	}

	def getOrElse(key: String, e: => V): V = this.synchronized {
		m.getOrElse(key, e)
	}

	def ++(t: Traversable[(String, List[V])]): scala.collection.mutable.Map[String, Any] = this.synchronized { m.clone } ++ t
	def contains(key: String) = this.synchronized {
		m.contains(key)
	}

	override def clone = this.synchronized { new LowerCaseMutableMap(m) }
	def cloneMap = this.synchronized { m.clone }

	override def toString = "LowerCaseMutableMap(%s)".format(m)
}