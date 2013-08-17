package com.googlecode.mapperdao.queryphase

import com.googlecode.mapperdao.queryphase.model._
import com.googlecode.mapperdao.schema.Type
import com.googlecode.mapperdao.schema.ManyToMany

/**
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
class QueryPhase
{
	def toQuery[ID, T](tpe: Type[ID, T]): Select = {
		val from = From(InQueryTable(Table(tpe.table), "maint"))
		Select(from, joins(tpe))
	}

	private def joins[ID, T](tpe: Type[ID, T]) = {
		tpe.table.relationshipColumns.map {
			case ManyToMany(e, linkTable, foreign) =>
				Join(
					Table(linkTable),
					OnClause(
						tpe.table.primaryKeys.map {
							pk =>
								Column(pk.name)
						},
						linkTable.left.map {
							c =>
								Column(c.name)
						}
					)
				)
		}
	}
}
