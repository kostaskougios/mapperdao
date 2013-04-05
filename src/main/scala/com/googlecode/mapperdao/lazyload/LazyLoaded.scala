package com.googlecode.mapperdao.lazyload

/**
 * this is mixed in all lazy loaded entities by LazyLoadManager
 *
 * @author kostantinos.kougios
 *
 *         May 24, 2012
 */
protected trait LazyLoaded
{
	def freeLazyLoadMemoryData()
}