package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
case class LazyLoad(all: Boolean = true)

object LazyLoad {
	// dont lazy load anything
	val none = LazyLoad(false)
	// lazy load all related entities
	val all = LazyLoad(all = true)
}