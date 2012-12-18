package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._

/**
 * base for all persist operations
 *
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
trait PersistCmd

trait CmdWithEntity[ID, T] extends PersistCmd {
	val entity: Entity[ID, DeclaredIds[ID], T]
}

