package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao.state.persistcmds.{RelatedCmd, PersistCmd}
import com.googlecode.mapperdao.ValuesMap

/**
 * prioritized cmds
 *
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class Prioritized(
	high: List[List[PersistCmd]],
	low: List[PersistCmd],
	related: List[RelatedCmd]
) {
	private val relatedById = related.groupBy(_.vm.identity)

	def relatedFor(vm: ValuesMap) = relatedById.getOrElse(vm.identity, Nil)
}
