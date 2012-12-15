package com.googlecode.mapperdao.utils

import java.util.IdentityHashMap

/**
 * @author kostantinos.kougios
 *
 * 15 Dec 2012
 */
class IdentityMap[T, V] {
	private val m = new IdentityHashMap[T, V]

	def +(t: T, v: V) = m.put(t, v)
	def apply(t: T) = {
		val v = m.get(t)
		if (v == null) throw new IllegalArgumentException("invalid key " + t)
		v
	}

}