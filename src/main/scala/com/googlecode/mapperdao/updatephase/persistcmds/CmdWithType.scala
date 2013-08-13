package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.schema.Type

/**
 * @author kostantinos.kougios
 *
 *         18 Dec 2012
 */
trait CmdWithType[ID, T] extends PersistCmd
{
	val tpe: Type[ID, T]
}