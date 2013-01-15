package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, Priority}
import com.googlecode.mapperdao.ColumnInfoTraversableManyToMany

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class DeleteManyToManyExternalCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToMany: ColumnInfoTraversableManyToMany[T, FID, FT],
	entityVM: ValuesMap,
	fo: FT
	) extends PersistCmd {
	def contributes(pri: Prioritized) = Contribute.StorageOnly

	def priority = Priority.Low
}
