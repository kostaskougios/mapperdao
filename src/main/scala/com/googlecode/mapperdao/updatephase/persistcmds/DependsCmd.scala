package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.updatephase.prioritise.Priority
import com.googlecode.mapperdao.ValuesMap

/**
 * a command that signals that an entity is dependent on an other one and the dependsOnIdentity should be
 * persisted before identity
 *
 * @author: kostas.kougios
 *          Date: 18/01/13
 */
case class DependsCmd(vm: ValuesMap, dependsOnVm: ValuesMap) extends PersistCmd
{
	def priority = Priority.Dependant
}
