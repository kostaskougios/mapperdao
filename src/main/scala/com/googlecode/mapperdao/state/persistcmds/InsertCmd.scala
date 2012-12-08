package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * an insert op for the specified entity
 *
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
case class InsertCmd[ID, PC <: DeclaredIds[ID], T](
	entity: Entity[ID, PC, T],
	o: T,
	columns: List[(SimpleColumn, Any)],
	commands: List[PersistCmd[_, _, _]]) extends PersistCmd[ID, PC, T]