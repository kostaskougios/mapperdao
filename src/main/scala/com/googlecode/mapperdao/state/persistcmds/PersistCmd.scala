package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.Priority

/**
 * base for all persist operations
 *
 * @author kostantinos.kougios
 *
 *         21 Nov 2012
 */
trait PersistCmd {
	def blank: Boolean

	def priority: Priority
}
