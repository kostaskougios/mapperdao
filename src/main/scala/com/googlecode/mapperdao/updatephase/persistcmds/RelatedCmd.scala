package com.googlecode.mapperdao.updatephase.persistcmds

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.{ColumnBase, Type}
import com.googlecode.mapperdao.updatephase.prioritise.Priority

import scala.language.{existentials, implicitConversions}

/**
 * a command that signals that an entity is related to an other entity
 *
 * @author kostas.kougios
 *         Date: 04/01/13
 */
trait RelatedCmd extends PersistCmd
{
	val column: ColumnBase
	val newVM: ValuesMap
	val oldVMO: Option[ValuesMap]
	val foreignTpe: Type[_, _]

	def priority = Priority.Related
}

case class EntityRelatedCmd(
	fo: Any,
	column: ColumnBase,
	newVM: ValuesMap,
	oldVMO: Option[ValuesMap],
	foreignTpe: Type[_, _],
	foreignVM: ValuesMap,
	oldForeignVMO: Option[ValuesMap],
	isKey: Boolean
	) extends RelatedCmd

case class ExternalEntityRelatedCmd(
	fo: Any,
	column: ColumnBase,
	newVM: ValuesMap,
	oldVMO: Option[ValuesMap],
	foreignTpe: Type[_, _],
	foreignKeys: List[Any],
	oldForeignKeys: Option[List[Any]]
	) extends RelatedCmd
