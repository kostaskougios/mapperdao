package com.rits.orm.utils
import com.rits.orm.ValuesMap

/**
 * immutable set that is mutable within orm package :)
 *
 * This is necessary to load immutable cyclic depended entities
 *
 * @author kostantinos.kougios
 *
 * 8 Aug 2011
 */
class ISet[A](private var map: ValuesMap, key: String) extends Set[A] {
	private var cache: Set[A] = _
	private var lastM = map.m
	private def set = {
		if (cache == null || map.m.ne(lastM)) {
			cache = map.set[A](key)
			lastM = map.m
		}
		cache
	}
	//protected[orm]
	def m = map
	protected[utils] def m_=(m: ValuesMap) {
		map = m
	}

	def contains(elem: A): Boolean = set.contains(elem)

	def +(elem: A): Set[A] = set + elem
	def -(elem: A): Set[A] = set - elem

	def iterator: Iterator[A] = set.iterator

}