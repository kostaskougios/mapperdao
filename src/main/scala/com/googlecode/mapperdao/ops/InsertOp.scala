package com.googlecode.mapperdao.ops

import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.Entity

/**
 * an insert op for the specified entity
 *
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
case class InsertOp[ID, PC <: DeclaredIds[ID], T](
	entity: Entity[ID, PC, T],
	o: T) extends PersistOp[ID, PC, T]