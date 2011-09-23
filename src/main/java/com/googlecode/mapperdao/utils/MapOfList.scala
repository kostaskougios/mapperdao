package com.googlecode.mapperdao.utils
import scala.collection.mutable.HashMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class MapOfList[K, V](keyModifier: K => K) extends Traversable[(K, List[V])] {
	val m = new HashMap[K, List[V]]

	def update(k: K, v: V) =
		{
			val key = keyModifier(k)
			var l = m.getOrElse(key, List[V]())
			l ::= v
			m(key) = l
			l
		}

	override def foreach[U](f: ((K, List[V])) => U): Unit =
		{
			m.foreach(e => f(keyModifier(e._1), e._2))
		}

}

object MapOfList {
	def stringToLowerCaseModifier(key: String) = key.toLowerCase()
}