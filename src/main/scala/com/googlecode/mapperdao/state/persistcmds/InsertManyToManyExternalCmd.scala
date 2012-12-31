package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Low

/**
 * @author: kostas.kougios
 *          Date: 27/12/12
 */
case class InsertManyToManyExternalCmd[ID, T, FID, FT](
	entity: Entity[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableManyToMany[T, FID, FT],
	entityVM: ValuesMap,
	foreignO: FT

) extends PersistCmd {
	def blank = false

	def priority = Low
}

