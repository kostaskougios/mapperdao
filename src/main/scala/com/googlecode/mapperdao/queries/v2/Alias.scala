package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase
import com.googlecode.mapperdao.schema.{LinkTable, ColumnBase}

/**
 * @author: kostas.kougios
 *          Date: 13/09/13
 */
case class Alias[ID, T](entity: EntityBase[ID, T], tableAlias: Symbol)

object Alias
{
	def apply[ID, T](entity: EntityBase[ID, T]): Alias[ID, T] = Alias(entity, aliasFor(entity))

	def aliasFor(column: ColumnBase): Symbol = aliasFor(column.entity)

	def aliasFor[ID, T](entity: EntityBase[ID, T]): Symbol = {
		val a = prefix(entity.tableLower) + entity.entityId
		Symbol(a)
	}

	def aliasFor[ID, T](linkTable: LinkTable): Symbol =
		Symbol(prefix(linkTable.name))

	private def prefix(name: String) = name.substring(0, math.min(2, name.length))
}