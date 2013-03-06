package com.googlecode.mapperdao.internal

/**
 * @author kostantinos.kougios
 *
 *         Jan 31, 2012
 */
private[mapperdao] class MapWithDefault[K, V](notFoundMsg: String) {
	private var m: Map[K, V] = Map()
	var default: Option[V] = None

	def +(k: K, v: V) {
		m += (k -> v)
	}

	def apply(key: K): V = m.get(key).getOrElse(default.getOrElse(throw new IllegalStateException(notFoundMsg)))
}