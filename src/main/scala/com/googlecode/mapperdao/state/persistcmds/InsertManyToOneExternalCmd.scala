package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.prioritise.{Low, Prioritized}

/**
 * @author: kostas.kougios
 *          Date: 09/01/13
 */
case class InsertManyToOneExternalCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	manyToOne: ColumnInfoManyToOne[T, FID, FT],
	entityVM: ValuesMap,
	foreignO: FT
) extends PersistCmd {
	def blank(pri: Prioritized) = false

	def priority = Low
}

