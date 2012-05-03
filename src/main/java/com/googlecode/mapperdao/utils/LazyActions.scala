package com.googlecode.mapperdao.utils

/**
 * queues a piece of code and executes all queue later on
 *
 * @author kostantinos.kougios
 *
 * 29 Jan 2012
 */
class LazyActions {
	type Action = () => Unit
	private var actions = List[Action]()

	def apply(action: Action): Unit = actions = action :: actions

	def executeAll: Unit = actions.foreach { _() }

	override def toString = "LazyActions(%s)".format(actions)
}