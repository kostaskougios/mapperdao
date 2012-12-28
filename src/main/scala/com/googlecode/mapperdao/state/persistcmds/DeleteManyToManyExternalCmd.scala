package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.prioritise.Low
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class DeleteManyToManyExternalCmd[ID, T, FID, FT](
	entity: Entity[ID, DeclaredIds[ID], T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableManyToMany[T, FID, _ <: DeclaredIds[FID], FT],
	entityVM: ValuesMap,
	fo: FT
) extends PersistCmd {
	def blank = false

	def priority = Low
}
