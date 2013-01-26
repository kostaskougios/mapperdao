package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Priority

/**
 * signals that there must be a link insert between 2 entities, one of them been an external entity
 *
 * @author: kostas.kougios
 *          Date: 27/12/12
 */
case class InsertOneToManyExternalCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableOneToMany[ID, T, FID, FT],
	entityVM: ValuesMap,
	foreignO: FT

	) extends PersistCmd {
	def priority = Priority.Low
}

