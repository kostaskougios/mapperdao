package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
case class LazyLoad(
	all: Boolean = true,
	lazyLoaded: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set()) {
	if (all && !lazyLoaded.isEmpty) throw new IllegalStateException("all=true but lazyLoaded is also set to %s".format(lazyLoaded))

	def isLazyLoaded(ci: ColumnInfoBase[_, _]) = all || (ci match {
		case ci: ColumnInfoRelationshipBase[_, _, _, _] =>
			lazyLoaded.contains(ci)
		case _ => false
	})

	def isAnyColumnLazyLoaded(cis: Set[ColumnInfoRelationshipBase[_, _, _, _]]) = all || !lazyLoaded.intersect(cis).isEmpty
}

object LazyLoad {
	// dont lazy load anything
	val none = LazyLoad(false)
	// lazy load all related entities
	val all = LazyLoad(all = true)
}