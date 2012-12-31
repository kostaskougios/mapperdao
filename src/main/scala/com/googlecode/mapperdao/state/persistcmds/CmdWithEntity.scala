package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao.Entity

/**
 * @author kostantinos.kougios
 *
 *         18 Dec 2012
 */
trait CmdWithEntity[ID, T] extends PersistCmd {
	val entity: Entity[ID, T]
}