package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.state.prioritise.{Priority, Prioritized}

/**
 * @author: kostas.kougios
 *          Date: 1/15/13
 */
case class UpdateExternalManyToOneCmd[FID, FT](
	foreignEntity: ExternalEntity[FID, FT],
	fo: FT
	) extends PersistCmd {
	def blank(pri: Prioritized) = true

	def priority = Priority.Low
}