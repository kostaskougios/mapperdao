package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Low

/**
 * @author kostas.kougios
 *         23/12/12
 */
case class DeleteManyToManyCmd[ID, T, FID, FT](
	entity: Entity[ID, T],
	foreignEntity: Entity[FID, FT],
	manyToMany: ManyToMany[FID, FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap
) extends PersistCmd {
	def blank = false

	def priority = Low
}
