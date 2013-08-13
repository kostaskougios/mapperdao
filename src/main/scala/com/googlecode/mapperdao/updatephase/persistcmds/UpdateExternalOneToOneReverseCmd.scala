package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.{ValuesMap, ExternalEntity}
import com.googlecode.mapperdao.updatephase.prioritise.Priority
import com.googlecode.mapperdao.schema.{Type, ColumnInfoOneToOneReverse}

/**
 * @author: kostas.kougios
 *          Date: 28/02/13
 */
case class UpdateExternalOneToOneReverseCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignEntity: ExternalEntity[FID, FT],
	oneToOne: ColumnInfoOneToOneReverse[ID, FID, FT],
	entityVM: ValuesMap,
	oldT: FT,
	newT: FT
	) extends PersistCmd
{
	def priority = Priority.Low
}
