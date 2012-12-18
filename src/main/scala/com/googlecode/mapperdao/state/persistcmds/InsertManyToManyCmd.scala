package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 18 Dec 2012
 */
case class InsertManyToManyCmd[ID, T, FID, FT](
	entity: Entity[ID, DeclaredIds[ID], T],
	foreignEntity: Entity[FID, DeclaredIds[FID], FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap) extends CmdWithEntity[ID, T]