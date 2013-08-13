package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.updatephase.prioritise.Priority
import com.googlecode.mapperdao.schema.Type

/**
 * signals a delete
 *
 * @author: kostas.kougios
 *          Date: 1/16/13
 */
case class DeleteCmd[ID, T](
	tpe: Type[ID, T],
	newVM: ValuesMap
	) extends CmdWithType[ID, T]
{

	def priority = Priority.High
}
