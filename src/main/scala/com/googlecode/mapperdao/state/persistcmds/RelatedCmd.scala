package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, Related}

/**
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
case class RelatedCmd(
	column: ColumnBase,
	vm: ValuesMap,
	foreignTpe: Type[_, _],
	foreignVM: ValuesMap
) extends PersistCmd {
	def blank(pri: Prioritized) = true

	def priority = Related
}
