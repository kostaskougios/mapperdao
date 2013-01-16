package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, Priority}

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
	def contributes(pri: Prioritized) =
		if (columns.isEmpty && pri.relatedColumns(newVM).isEmpty)
			Contribute.NoContribution
		else
			Contribute.StorageOnly

	def priority = Priority.High
}