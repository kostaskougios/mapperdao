package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 18 Dec 2012
 */
trait CmdWithEntity[ID, T] extends PersistCmd {
	val entity: Entity[ID, DeclaredIds[ID], T]
}