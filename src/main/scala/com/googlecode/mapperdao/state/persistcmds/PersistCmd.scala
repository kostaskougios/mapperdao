package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.{Prioritized, Priority}

/**
 * base for all persist operations
 *
 * @author kostantinos.kougios
 *
 *         21 Nov 2012
 */
trait PersistCmd {
	def blank(pri: Prioritized): Boolean

	def priority: Priority
}
