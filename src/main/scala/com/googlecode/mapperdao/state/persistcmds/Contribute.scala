package com.googlecode.mapperdao.state.persistcmds

/**
 * @author: kostas.kougios
 *          Date: 1/15/13
 */
trait Contribute

object Contribute {

	object Storage extends Contribute

	val StorageOnly = Set[Contribute](Contribute.Storage)
}
