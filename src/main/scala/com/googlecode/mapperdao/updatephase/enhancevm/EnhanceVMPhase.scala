package com.googlecode.mapperdao.updatephase.enhancevm

import com.googlecode.mapperdao.updatephase.prioritise.Prioritized
import com.googlecode.mapperdao.updatephase.persistcmds.{UpdateCmd, InsertCmd}

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
