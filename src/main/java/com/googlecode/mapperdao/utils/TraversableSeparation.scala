package com.googlecode.mapperdao.utils

import java.util.IdentityHashMap
import java.util.TreeMap
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.SimpleTypeValue
import com.googlecode.mapperdao.Persisted

/**
 * compares 2 traversables via object reference equality and returns (added,intersect,removed)
 *
 * @author kostantinos.kougios
 *
 * 6 Sep 2011
 */
protected[mapperdao] object TraversableSeparation {
	def separate[T](entity: Entity[_, T], oldT: Traversable[T], newT: Traversable[T]) =
		{
			if (oldT.isEmpty)
				(newT, Nil, Nil)
			else if (newT.isEmpty)
				(Nil, Nil, oldT)
			else {
				val (oldM, newM) = oldT.head match {
					case _: SimpleTypeValue[T, _] =>
						val eq = new EntityMap.ByObjectEquals[T]
						(new EntityMap(entity, eq), new EntityMap(entity, eq))
					case _ =>
						val eq = new EntityMap.EntityEquals(entity)
						(new EntityMap(entity, eq), new EntityMap(entity, eq))

				}
				oldM.addAll(oldT)
				newM.addAll(newT)

				val added = newT.filterNot(oldM.contains(_))
				val intersect = oldT.filter(newM.contains(_)).map(ot => (ot.asInstanceOf[T with Persisted], newM(ot)))
				val removed = oldT.filterNot(newM.contains(_))

				(added, intersect, removed)
			}
		}
}