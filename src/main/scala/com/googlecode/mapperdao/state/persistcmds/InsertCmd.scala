package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.High

/**
 * an insert op for the specified entity
 *
 * @author kostantinos.kougios
 *
 *         21 Nov 2012
 */
case class InsertCmd[ID, T](
	tpe: Type[ID, T],
	newVM: ValuesMap,
	columns: List[(SimpleColumn, Any)],
	mainEntity: Boolean
) extends CmdWithEntity[ID, T] with CmdWithNewVM {
	def blank = columns.isEmpty

	def priority = High
}