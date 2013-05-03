package com.googlecode.mapperdao.internal

import java.util

/**
 * @author: kostas.kougios
 *          Date: 02/05/13
 */
private[mapperdao] class MutableIdentityHashMap[K, V]
{
	private val m = new util.IdentityHashMap[K, V]

	def apply(k: K) = m.get(k) match {
		case null => throw new IllegalStateException("key not found : " + k)
		case v => v
	}

	def get(k: K) = m.get(k) match {
		case null => None
		case x => Some(x)
	}

	def update(k: K, v: V) {
		m.put(k, v)
	}
}
