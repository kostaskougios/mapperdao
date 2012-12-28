package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.Low

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class UpdateExternalCmd[FT](fo: FT) extends PersistCmd {
	def blank = false

	def priority = Low
}