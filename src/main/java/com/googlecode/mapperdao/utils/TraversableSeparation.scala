package com.googlecode.mapperdao.utils
import java.util.IdentityHashMap

/**
 * compares 2 traversables via object reference equality and returns (added,intersect,removed)
 *
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
protected[mapperdao] object TraversableSeparation {
	def separate[T](oldT: Traversable[T], newT: Traversable[T]) =
		{
			if (oldT.isEmpty)
				(newT, Nil, Nil)
			else if (newT.isEmpty)
				(Nil, Nil, oldT)
			else {
				val oldM = new IdentityHashMap[T, T]
				val newM = new IdentityHashMap[T, T]

				oldT.foreach { item =>
					oldM.put(item, item)
				}

				newT.foreach { item =>
					newM.put(item, item)
				}

				val added = newT.filterNot(oldM.containsKey(_))
				val intersect = newT.filter(oldM.containsKey(_))
				val removed = oldT.filterNot(newM.containsKey(_))

				(added, intersect, removed)
			}
		}
}