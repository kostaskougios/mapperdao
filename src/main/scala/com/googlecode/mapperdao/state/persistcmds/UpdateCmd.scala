package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, High}

/**
 * @author kostantinos.kougios
 *
 *         Dec 9, 2012
 */
case class UpdateCmd[ID, T](
	tpe: Type[ID, T],
	oldVM: ValuesMap,
	newVM: ValuesMap,
	columns: List[(SimpleColumn, Any)],
	mainEntity: Boolean
) extends CmdWithType[ID, T] with CmdWithNewVM {
	def blank(pri: Prioritized) = columns.isEmpty && pri.relatedColumns(newVM).isEmpty

	def priority = High
}