package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.updatephase.prioritise.Priority
import com.googlecode.mapperdao.schema.Type

/**
 * @author: kostas.kougios
 *          Date: 22/02/13
 */
case class MockCmd[ID, T](
	tpe: Type[ID, T],
	oldVM: ValuesMap,
	newVM: ValuesMap
	) extends PersistCmd
{
	def priority = Priority.Low
}