package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Related

/**
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class RelatedCmd(vm: ValuesMap, foreignVM: ValuesMap) extends PersistCmd {
	def blank = true

	def priority = Related
}
