package com.googlecode.mapperdao

/**
 * configuration for running queries using multiple
 * threads.
 *
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
abstract class MultiThreadedConfig {
	val runInParallel: Boolean
}

object MultiThreadedConfig {
	object Single extends MultiThreadedConfig {
		val runInParallel = false
	}

	object Multi extends MultiThreadedConfig {
		val runInParallel = true
	}
}