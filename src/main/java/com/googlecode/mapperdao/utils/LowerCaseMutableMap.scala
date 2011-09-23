package com.googlecode.mapperdao.utils
import scala.collection.mutable.HashMap

/**
 * @author kostantinos.kougios
 *
 * 23 Sep 2011
 */
class LowerCaseMutableMap[V] {
	private val m = new HashMap[String, V]
	def this(map: scala.collection.mutable.Map[String, V]) =
		{
			this()
			map.foreach(t => m(t._1.toLowerCase) = t._2)
		}

	def get(key: String): Option[V] = m.get(key.toLowerCase)
	def update(key: String, v: V) = m(key.toLowerCase) = v
	def apply(key: String) = m(key.toLowerCase)
	def getOrElse(key: String, e: => V) = m.getOrElse(key, e)
	def ++(t: Traversable[(String, List[V])]) = m.clone ++ t

	override def clone = new LowerCaseMutableMap(m)
	def cloneMap = m.clone
}