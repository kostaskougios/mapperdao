package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao.ValuesMap

/**
 * @author: kostas.kougios
 *          Date: 26/12/12
 */
trait CmdWithNewVM
{
	val newVM: ValuesMap
}
