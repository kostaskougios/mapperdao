package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao._
import updatephase.prioritise.Priority
import com.googlecode.mapperdao.schema.{Type, SimpleColumn}

/**
 * signals an update for an entity
 *
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
	) extends CmdWithType[ID, T] with CmdWithNewVM
{
	def priority = Priority.High

	val columnNames = columns.map(_._1.name).toSet
}