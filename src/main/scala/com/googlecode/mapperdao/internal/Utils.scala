package com.googlecode.mapperdao.internal

import java.util

/**
 * @author: kostas.kougios
 *          Date: 05/03/13
 */
private[mapperdao] object Utils {

	/**
	 * converts a traversable to a hashSet
	 */
	def toJavaSet[T](t: Traversable[T]): util.Set[T] = {
		val s = new util.HashSet[T]
		copy(t, s)
		s
	}

	/**
	 * converts a traversable to an ArrayList
	 */
	def toJavaList[T](t: Traversable[T]): util.List[T] = {
		val a = new util.ArrayList[T]
		copy(t, a)
		a
	}

	def copy[T](t: Traversable[T], c: util.Collection[T]) {
		t.foreach {
			i =>
				c.add(i)
		}
	}
}
