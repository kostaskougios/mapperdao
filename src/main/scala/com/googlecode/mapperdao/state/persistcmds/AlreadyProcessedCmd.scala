package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.{Priority, Prioritized}


/**
 * @author kostantinos.kougios
 *
 *         22 Nov 2012
 */
case object AlreadyProcessedCmd extends PersistCmd {
	def contributes(pri: Prioritized) = Set()

	def priority = Priority.Low
}