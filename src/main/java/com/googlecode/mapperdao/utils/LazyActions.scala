package com.googlecode.mapperdao.utils

/**
 * queues a piece of code and executes all queue later on
 *
 * @author kostantinos.kougios
 *
 * 29 Jan 2012
 */
private[mapperdao] class LazyActions[T] {
	type Action = () => T
	private var actions = List[Action]()

	def apply(action: Action): Unit = actions = action :: actions

	def executeAll: List[T] = actions.map { _() }

	override def toString = "LazyActions(%s)".format(actions)
}