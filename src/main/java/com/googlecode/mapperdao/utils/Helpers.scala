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
			val intersection = oldSet.intersect(newSet)
			val added = newSet.filterNot(oldSet.contains(_))
			intersection ++ added
		}

	def modified[T](oldList: List[T], newList: List[T]): List[T] =
		{
			val ml = new collection.mutable.ArrayBuffer ++ oldList
			newList.map { item =>
				ml.find(_ == item) match {
					case Some(ni) =>
						ml -= ni
						ni
					case None => item
				}
			}
		}
}