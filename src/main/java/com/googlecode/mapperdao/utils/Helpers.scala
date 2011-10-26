package com.googlecode.mapperdao.utils

/**
 * useful methods for real life applications that use
 * mapperdao.
 *
 * @author kostantinos.kougios
 *
 * 26 Oct 2011
 */
object Helpers {
	def modified[T](oldSet: Set[T], newSet: Set[T]): Set[T] =
		{
			val removed = oldSet.filterNot(newSet.contains(_))
		}
}