package com.googlecode.mapperdao.state.prioritise

import com.googlecode.mapperdao.state.persistcmds.{RelatedCmd, PersistCmd}

/**
 * prioritized cmds
 *
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class Prioritized(
	high: List[List[PersistCmd]],
	low: List[PersistCmd],
	related: List[PersistCmd]
) {
	val relatedById = related.map {
		case r: RelatedCmd => (r.vm.identity, r)
	}.toMap
}
