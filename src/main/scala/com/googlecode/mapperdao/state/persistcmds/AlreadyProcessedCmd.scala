package com.googlecode.mapperdao.state.persistcmds


/**
 * @author kostantinos.kougios
 *
 *         22 Nov 2012
 */
case object AlreadyProcessedCmd extends PersistCmd {
	def blank = true
}