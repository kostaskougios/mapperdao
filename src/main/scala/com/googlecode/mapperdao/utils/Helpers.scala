package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao._

/**
 * useful methods for real life applications that use
 * mapperdao.
 *
 * @author kostantinos.kougios
 *
 *         26 Oct 2011
 */
object Helpers
{

	/**
	 * tests any instance to find out if it is a persisted one
	 *
	 * @param o		the instance of the entity
	 * @return		true if the entity was loaded or inserted or updated, false if it
	 *                is a plain unlinked instance
	 */
	def isPersisted(o: Any) = o match {
		case p: Persisted if (p.mapperDaoValuesMap != null) => true
		case _ => false
	}

	/**
	 * returns the id of an IntId entity or throws an exception if the entity
	 * is not persisted or not of IntId
	 */
	def intIdOf(o: Any): Int = o match {
		case i: SurrogateIntId => i.id
		case i: SurrogateIntAndNaturalStringId => i.id
		case _ => throw new IllegalArgumentException("not an IntId : " + o.toString)
	}

	/**
	 * returns the id of a LongId entity or throws an exception if the entity
	 * is not persisted or not of IntId
	 */
	def longIdOf(o: Any): Long = o match {
		case i: SurrogateLongId => i.id
		case i: SurrogateLongAndNaturalStringId => i.id
		case _ => throw new IllegalArgumentException("not an LongId : " + o.toString)
	}

	/**
	 * when loading an NoId entity from the database, the type is T with NoId. If for
	 * some reason we're sure that the entity T is of NoId, we can easily cast it
	 * using this utility method
	 */
	def asNoId[T](t: T) = t.asInstanceOf[T with NoId]

	/**
	 * when loading an IntId entity from the database, the type is T with IntId. If for
	 * some reason we're sure that the entity T is of IntId, we can easily cast it
	 * using this utility method
	 */
	def asSurrogateIntId[T](t: T) = t.asInstanceOf[T with SurrogateIntId]

	def asNaturalIntId[T](t: T) = t.asInstanceOf[T with NaturalIntId]

	/**
	 * when loading an LongId entity from the database, the type is T with LongId. If for
	 * some reason we're sure that the entity T is of LongId, we can easily cast it
	 * using this utility method
	 */
	def asSurrogateLongId[T](t: T) = t.asInstanceOf[T with SurrogateLongId]

	def asNaturalLongId[T](t: T) = t.asInstanceOf[T with NaturalLongId]

	def asNaturalStringId[T](t: T) = t.asInstanceOf[T with NaturalStringId]

	/**
	 * merges oldSet and newSet items, keeping all unmodified
	 * items from oldSet and adding all newItems from newSet.
	 * If t1 belongs to oldSet and t1e==t1 belongs to newSet,
	 * then t1 will be retained. This helps with collection
	 * updates, as 2 sets (old and new) can be merged before
	 * instantiating updated entities.
	 *
	 * @param oldSet		the set containing old values
	 * @param newSet		the set, updated with new values
	 * @return				merged set which == newSet but contains
	 *                        instances from oldSet where appropriate.
	 */
	def merge[T](oldSet: Set[T], newSet: Set[T]): Set[T] = {
		val intersection = oldSet.intersect(newSet)
		val added = newSet.filterNot(oldSet.contains(_))
		intersection ++ added
	}

	/**
	 * merges the 2 lists with result==newList but result
	 * retaining all instances of oldList that are contained
	 * in newList. This helps with mapperdao collection
	 * updates, provided that all contained instances
	 * impl equals().
	 *
	 * This method also retains the order of the items
	 * (as is in newList) and duplicate items that are
	 * contained both in newList and oldList
	 *
	 * @param oldList		the list of items before the update
	 * @param newList		the list of items after the update,
	 *                       all of them might be new instances
	 *                       but equal() to items in oldList
	 * @return				the merged list, where merged==newList
	 *                        but retains all instances from oldList
	 *                        that are contained in newList
	 */
	def merge[T](oldList: List[T], newList: List[T]): List[T] = {
		val ml = new collection.mutable.ArrayBuffer ++ oldList
		newList.map {
			item =>
				ml.find(_ == item) match {
					case Some(ni) =>
						ml -= ni
						ni
					case None => item
				}
		}
	}

	def idToList[ID](id: ID): List[Any] = id match {
		case i: Int => List(i)
		case l: Long => List(l)
		case s: String => List(s)
		case (a1, a2) => List(a1, a2)
		case (a1, a2, a3) => List(a1, a2, a3)
		case (a1, a2, a3, a4) => List(a1, a2, a3, a4)
		case (a1, a2, a3, a4, a5) => List(a1, a2, a3, a4, a5)
		case (a1, a2, a3, a4, a5, a6) => List(a1, a2, a3, a4, a5, a6)
		case (a1, a2, a3, a4, a5, a6, a7) => List(a1, a2, a3, a4, a5, a6, a7)
		case a => List(a)
	}

	/**
	 * releases the objects used to store state for lazy loading to occur, freeing memory
	 * but lazy loaded relationships will not be loaded if their fields are accessed.
	 */
	def unlinkLazyLoadMemoryData[ID,PC<:Persisted, T](entity: Entity[ID,PC, T], o: T) {
		val visitor = new FreeLazyLoadedEntityVisitor
		visitor.visit(entity, o)
		visitor.free(o)
	}
}