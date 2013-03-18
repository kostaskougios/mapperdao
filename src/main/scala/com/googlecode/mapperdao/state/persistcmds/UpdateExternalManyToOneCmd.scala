package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.{ColumnInfoManyToOne, ValuesMap, ExternalEntity}
import com.googlecode.mapperdao.state.prioritise.Priority

/**
 * signals an update which links an entity with an other entity
 *
 * @author: kostas.kougios
 *          Date: 1/15/13
 */
case class UpdateExternalManyToOneCmd[FID, FT](
	foreignEntity: ExternalEntity[FID, FT],
	ci: ColumnInfoManyToOne[_, FID, FT],
	newVM: ValuesMap,
	fo: FT
	) extends PersistCmd
{
	def priority = Priority.Low
}