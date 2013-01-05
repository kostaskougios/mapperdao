package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.{Prioritized, Low}


/**
 * @author kostantinos.kougios
 *
 *         22 Nov 2012
 */
case object AlreadyProcessedCmd extends PersistCmd {
	def blank(pri: Prioritized) = true

	def priority = Low
}