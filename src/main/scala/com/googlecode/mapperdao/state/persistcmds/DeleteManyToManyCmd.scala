package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, Priority}

/**
 * @author kostas.kougios
 *         23/12/12
 */
case class DeleteManyToManyCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignTpe: Type[FID, FT],
	manyToMany: ManyToMany[FID, FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap
	) extends PersistCmd {
	def contributes(pri: Prioritized) = Contribute.StorageOnly

	def priority = Priority.Low
}
