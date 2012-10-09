package com.googlecode.mapperdao.utils
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
private[mapperdao] class MapOfList[K, V](keyModifier: K => K) extends Traversable[(K, List[V])] {
	val m = new HashMap[K, ListBuffer[V]]

	def update(k: K, v: V) {
		val key = keyModifier(k)
		val l = m.getOrElse(key, ListBuffer())
		if (l.isEmpty) m.put(key, l)
		l += v
	}

	override def foreach[U](f: ((K, List[V])) => U): Unit =
		m.foreach { e =>
			f(keyModifier(e._1), e._2.toList)
		}

}

object MapOfList {
	def stringToLowerCaseModifier(key: String) = key.toLowerCase
}