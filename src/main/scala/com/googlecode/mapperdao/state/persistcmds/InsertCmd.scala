package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * an insert op for the specified entity
 *
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
case class InsertCmd[ID, T](
	entity: Entity[ID, DeclaredIds[ID], T],
	newVM: ValuesMap,
	columns: List[(SimpleColumn, Any)],
	commands: List[PersistCmd[_, _]]) extends PersistCmd[ID, T]