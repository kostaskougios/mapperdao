package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * Dec 9, 2012
 */
case class UpdateCmd[ID, T](
	entity: Entity[ID, DeclaredIds[ID], T],
	o: T,
	oldVM: ValuesMap,
	columns: List[(SimpleColumn, Any)],
	commands: List[PersistCmd[_, _]]) extends PersistCmd[ID, T]