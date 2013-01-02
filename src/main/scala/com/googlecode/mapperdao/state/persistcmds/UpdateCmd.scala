package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.High

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
	def blank = columns.isEmpty

	def priority = High
}