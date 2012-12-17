package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 * 22 Nov 2012
 */
case class AlreadyProcessedCmd[ID, T](
		entity: Entity[ID, DeclaredIds[ID], T]) extends PersistCmd[ID, T] {
	val commands = Nil
}