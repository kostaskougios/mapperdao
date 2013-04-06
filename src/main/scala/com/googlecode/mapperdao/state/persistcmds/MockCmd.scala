package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.state.prioritise.Priority
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

	def identity = newVM.identity
}