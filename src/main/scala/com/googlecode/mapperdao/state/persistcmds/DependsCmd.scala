package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.Priority

/**
 * @author: kostas.kougios
 *          Date: 18/01/13
 */
case class DependsCmd(identity: Int, dependsOnIdentity: Int) extends PersistCmd {
	def priority = Priority.Dependant
}
