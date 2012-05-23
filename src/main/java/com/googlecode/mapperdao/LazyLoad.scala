package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
abstract class LazyLoad {
	def all: Boolean

	def lazyLoaded: Set[ColumnInfoRelationshipBase[_, _, _, _]]

	def isLazyLoaded(ci: ColumnInfoBase[_, _]) = all || (ci match {
		case ci: ColumnInfoRelationshipBase[_, _, _, _] =>
			lazyLoaded.contains(ci)
		case _ => false
	})

	def isAnyColumnLazyLoaded(cis: Set[ColumnInfoRelationshipBase[_, _, _, _]]) = all || !lazyLoaded.intersect(cis).isEmpty
}

case object LazyLoadNone extends LazyLoad {
	val all = false
	val lazyLoaded = Set[ColumnInfoRelationshipBase[_, _, _, _]]()
}

case object LazyLoadAll extends LazyLoad {
	val all = true
	val lazyLoaded = Set[ColumnInfoRelationshipBase[_, _, _, _]]()
}

case class LazyLoadSome(val lazyLoaded: Set[ColumnInfoRelationshipBase[_, _, _, _]]) extends LazyLoad {
	val all = false
}

object LazyLoad {
	// dont lazy load anything
	val none = LazyLoadNone
	// lazy load all related entities
	val all = LazyLoadAll

	def some(lazyLoaded: Set[ColumnInfoRelationshipBase[_, _, _, _]]) = LazyLoadSome(lazyLoaded = lazyLoaded)
}