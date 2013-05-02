package com.googlecode.mapperdao.internal

import java.util

/**
 * @author: kostas.kougios
 *          Date: 02/05/13
 */
private[mapperdao] class MutableIdentityHashSet[T]
{
	private val m = new util.IdentityHashMap[T, T]

	def apply(t: T) = m.containsKey(t)

	def +=(t: T) {
		m.put(t, t)
	}

}
