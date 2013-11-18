package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, OpBase}
import com.googlecode.mapperdao.Query.AscDesc
import com.googlecode.mapperdao.schema.ColumnBase

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class QueryInfo[ID, T](
	entityAlias: Alias[ID, T],
	wheres: Option[OpBase] = None,
	joins: List[Join] = Nil,
	order: List[(AliasColumn[_], AscDesc)] = Nil,
	aliases: Map[EntityBase[_, _], Symbol] = Map()
	)
{
	def aliasFor(column: ColumnBase) = {
		val entity = column.entity
		aliases.get(entity) match {
			case None =>
				val len = entity.tableLower.length
				val prefix = entity.tableLower.substring(0, math.min(2, len))
				val a = prefix + entity.entityId
				Symbol(a)
		}
	}
}
