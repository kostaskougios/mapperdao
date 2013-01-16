package com.googlecode.mapperdao.state.persistcmds

import com.googlecode.mapperdao._
import state.prioritise.{Prioritized, Priority}

/**
 * @author: kostas.kougios
 *          Date: 04/01/13
 */
trait RelatedCmd extends PersistCmd {
	val column: ColumnBase
	val vm: ValuesMap
	val foreignTpe: Type[_, _]

	def contributes(pri: Prioritized) = Contribute.NoContribution

	def priority = Priority.Related
}

case class EntityRelatedCmd(
	column: ColumnBase,
	vm: ValuesMap,
	foreignTpe: Type[_, _],
	foreignVM: ValuesMap
	) extends RelatedCmd

case class ExternalEntityRelatedCmd(
	column: ColumnBase,
	vm: ValuesMap,
	foreignTpe: Type[_, _],
	foreignKeys: PrimaryKeysValues
	) extends RelatedCmd
