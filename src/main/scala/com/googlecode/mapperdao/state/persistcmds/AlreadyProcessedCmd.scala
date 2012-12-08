package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 22 Nov 2012
 */
case class AlreadyProcessedCmd[ID, PC <: DeclaredIds[ID], T](
	entity: Entity[ID, PC, T],
	o: T) extends PersistCmd[ID, PC, T]