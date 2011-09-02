package com.googlecode.mapperdao.utils
import scala.collection.mutable.HashMap

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class MapOfList[K, V] extends Traversable[(K, List[V])] {
	val m = new HashMap[K, List[V]]

	def update(k: K, v: V) =
		{
			var l = m.getOrElse(k, List[V]())
			l ::= v
			m(k) = l
			l
		}

	override def foreach[U](f: ((K, List[V])) => U): Unit =
		{
			m.foreach(e => f(e._1, e._2))
		}

}