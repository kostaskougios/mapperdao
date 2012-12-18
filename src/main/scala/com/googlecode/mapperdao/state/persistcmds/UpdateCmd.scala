package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * Dec 9, 2012
 */
case class UpdateCmd[ID, T](
	entity: Entity[ID, DeclaredIds[ID], T],
	oldVM: ValuesMap,
	newVM: ValuesMap,
	columns: List[(SimpleColumn, Any)]) extends CmdWithEntity[ID, T]