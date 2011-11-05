package com.googlecode.mapperdao.utils
import java.util.IdentityHashMap
import com.googlecode.mapperdao.SimpleTypeValue
import java.util.TreeMap

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
				val (oldM, newM) = oldT.head match {
					case _: SimpleTypeValue[T, _] =>
						// do an equals comparison
						(new TreeMap[T, T], new TreeMap[T, T])
					case _ =>
						// do an identity comparison
						(new IdentityHashMap[T, T], new IdentityHashMap[T, T])

				}
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