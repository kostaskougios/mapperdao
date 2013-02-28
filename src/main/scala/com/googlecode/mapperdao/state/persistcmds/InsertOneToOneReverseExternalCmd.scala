package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.{ColumnInfoOneToOneReverse, ValuesMap, ExternalEntity}
import com.googlecode.mapperdao.state.prioritise.Priority

/**
 * @author: kostas.kougios
 *          Date: 28/02/13
 */
case class InsertOneToOneReverseExternalCmd[ID, FID, FT](
	foreignEntity: ExternalEntity[FID, FT],
	oneToMany: ColumnInfoOneToOneReverse[ID, FID, FT],
	entityVM: ValuesMap,
	ft: FT
	) extends PersistCmd {
	def priority = Priority.Low
}
