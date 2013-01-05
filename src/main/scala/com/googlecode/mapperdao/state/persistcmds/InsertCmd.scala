package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, High}

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
) extends CmdWithType[ID, T] with CmdWithNewVM {
	def blank(pri: Prioritized) = columns.isEmpty && pri.relatedColumns(newVM).isEmpty

	def priority = High
}