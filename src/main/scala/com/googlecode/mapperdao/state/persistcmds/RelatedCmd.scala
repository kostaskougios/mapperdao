package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.Priority

/**
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
trait RelatedCmd extends PersistCmd {
	val column: ColumnBase
	val vm: ValuesMap
	val foreignTpe: Type[_, _]

	def priority = Priority.Related
}

case class EntityRelatedCmd(
	identity: Int,
	column: ColumnBase,
	vm: ValuesMap,
	foreignTpe: Type[_, _],
	foreignVM: ValuesMap,
	isKey: Boolean
	) extends RelatedCmd with CmdForEntity

case class ExternalEntityRelatedCmd(
	identity: Int,
	column: ColumnBase,
	vm: ValuesMap,
	foreignTpe: Type[_, _],
	foreignKeys: PrimaryKeysValues
	) extends RelatedCmd
