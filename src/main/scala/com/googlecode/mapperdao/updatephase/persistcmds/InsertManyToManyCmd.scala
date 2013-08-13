package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao._
import updatephase.prioritise.Priority
import com.googlecode.mapperdao.schema.{Type, ManyToMany}

/**
 * signals that there must be a link insert between 2 entities
 *
 * @author kostantinos.kougios
 *         18 Dec 2012
 */
case class InsertManyToManyCmd[ID, T, FID, FT](
	tpe: Type[ID, T],
	foreignTpe: Type[FID, FT],
	manyToMany: ManyToMany[FID, FT],
	entityVM: ValuesMap,
	foreignEntityVM: ValuesMap
	) extends PersistCmd
{
	def priority = Priority.Lowest
}