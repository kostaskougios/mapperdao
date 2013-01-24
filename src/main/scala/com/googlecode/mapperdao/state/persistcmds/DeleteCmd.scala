package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.{ValuesMap, Type}
import com.googlecode.mapperdao.state.prioritise.Priority

/**
 * @author: kostas.kougios
 *          Date: 1/16/13
 */
case class DeleteCmd[ID, T](
	tpe: Type[ID, T],
	newVM: ValuesMap
	) extends CmdWithType[ID, T] with CmdForEntity {

	def priority = Priority.High

	def identity = newVM.identity
}
