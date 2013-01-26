package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Priority

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class DeleteOneToManyExternalCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableOneToMany[ID, T, FID, FT],
	entityVM: ValuesMap,
	fo: FT
	) extends PersistCmd {
	def priority = Priority.Low
}
