package com.googlecode.mapperdao.state.enhancevm

import com.googlecode.mapperdao.state.prioritise.Prioritized
import com.googlecode.mapperdao.state.persistcmds.{UpdateCmd, InsertCmd}

/**
 * @author: kostas.kougios
 *          Date: 04/02/13
 */
class EnhanceVMPhase
{
	def execute(pri: Prioritized) {
		(pri.high.flatten ::: pri.low).foreach {
			case InsertCmd(tpe, newVM, columns, _) =>
				val related = pri.relatedColumns(newVM, true)
				newVM.addRelated(related)
			case UpdateCmd(tpe, oldVM, newVM, columns, _) =>
				val related = pri.relatedColumns(newVM, true)
				newVM.addRelated(related)
			case _ =>
		}
	}
}
