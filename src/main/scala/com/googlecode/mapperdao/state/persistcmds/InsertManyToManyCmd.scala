package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Low

/**
 * @author kostantinos.kougios
 *         18 Dec 2012
 */
case class InsertManyToManyCmd[ID, T, FID, FT](
	entity: Entity[ID, DeclaredIds[ID], T],
	foreignEntity: Entity[FID, DeclaredIds[FID], FT],
	manyToMany: ManyToMany[FID, _ <: DeclaredIds[FID], FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap
) extends PersistCmd {
	def blank = false

	def priority = Low
}