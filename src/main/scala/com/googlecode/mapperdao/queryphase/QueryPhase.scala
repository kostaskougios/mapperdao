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
		val iqt = InQueryTable(Table(tpe.table), "maint")
		val from = From(iqt)
		Select(from, joins(iqt, tpe))
	}

	private def joins[ID, T](iqt: InQueryTable, tpe: Type[ID, T]) = {
		tpe.table.relationshipColumns.map {
			case ManyToMany(e, linkTable, foreign) =>
				val linkT = Table(linkTable)
				val leftIQT = InQueryTable(linkT, alias)
				Join(
					leftIQT,
					OnClause(
						tpe.table.primaryKeys.map {
							pk =>
								Column(iqt, pk.name)
						},
						linkTable.left.map {
							c =>
								Column(leftIQT, c.name)
						}
					)
				)
		}
	}

	private var aliasCnt = 0

	private def alias = {
		aliasCnt += 1
		"a" + aliasCnt
	}
}
