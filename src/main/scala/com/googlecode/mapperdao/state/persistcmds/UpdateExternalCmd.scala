package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.state.prioritise.Low
import com.googlecode.mapperdao.{DeclaredIds, ColumnInfoTraversableManyToMany, ExternalEntity}

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class UpdateExternalCmd[T, FID, FT](
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableManyToMany[T, FID, _ <: DeclaredIds[FID], FT],
	fo: FT
) extends PersistCmd {
	def blank = false

	def priority = Low
}