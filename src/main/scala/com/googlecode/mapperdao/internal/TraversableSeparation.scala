package com.googlecode.mapperdao.internal

import com.googlecode.mapperdao._

/**
 * compares 2 traversables via object reference equality and returns
 * (added,intersect,removed)
 *
 * @author kostantinos.kougios
 *
 *         6 Sep 2011
 */
protected[mapperdao] object TraversableSeparation
{
	def separate[ID, T](entity: EntityBase[ID, T], oldT: Traversable[T], newT: Traversable[T]) = {
		if (oldT.isEmpty)
			(newT, Nil, Nil)
		else if (newT.isEmpty)
			(Nil, Nil, oldT)
		else {
			val (oldM, newM) = oldT.head match {
				case _: SimpleTypeValue[T, _] =>
					val eq = new EntityComparisonMap.ByObjectEquals[T]
					(new EntityComparisonMap(entity, eq), new EntityComparisonMap(entity, eq))
				case _ =>
					val eq = new EntityComparisonMap.EntityEquals(entity)
					(new EntityComparisonMap(entity, eq), new EntityComparisonMap(entity, eq))

			}
			oldM.addAll(oldT)
			newM.addAll(newT)

			val added = newT.filterNot(oldM.contains(_))
			val intersect = oldT.filter(newM.contains(_)).map(ot => (ot, newM(ot)))
			val removed = oldT.filterNot(newM.contains(_)).map(ot => ot.asInstanceOf[T with DeclaredIds[ID]])

			(added, intersect, removed)
		}
	}
}
