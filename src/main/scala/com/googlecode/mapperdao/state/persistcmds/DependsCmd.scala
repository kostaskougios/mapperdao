package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.{Priority, Prioritized}

/**
 * @author: kostas.kougios
 *          Date: 18/01/13
 */
case class DependsCmd(identity: Int, dependsOnIdentity: Int) extends PersistCmd {
	def contributes(pri: Prioritized) = Contribute.NoContribution

	def priority = Priority.Dependant
}
