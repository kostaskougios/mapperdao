package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Priority

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
	) extends CmdWithType[ID, T] with CmdWithNewVM with CmdForEntity {

	def priority = Priority.High

	def identity = newVM.identity
}
