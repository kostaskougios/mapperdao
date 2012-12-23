package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostas.kougios
 *         23/12/12
 */
case class DeleteManyToManyCmd[ID, T, FID, FT](
	entity: Entity[ID, DeclaredIds[ID], T],
	foreignEntity: Entity[FID, DeclaredIds[FID], FT],
	manyToMany: ManyToMany[FID, _ <: DeclaredIds[FID], FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap
) extends PersistCmd {
	def blank = false
}
