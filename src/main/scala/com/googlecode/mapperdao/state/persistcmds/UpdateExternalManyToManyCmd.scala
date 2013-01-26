package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Priority

/**
 * signals an update which links an entity with an external entity
 *
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class UpdateExternalManyToManyCmd[T, FID, FT](
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableManyToMany[T, FID, FT],
	fo: FT
	) extends PersistCmd {
	def priority = Priority.Low
}