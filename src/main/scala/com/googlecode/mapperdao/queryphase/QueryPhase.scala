package com.googlecode.mapperdao.queryphase

import com.googlecode.mapperdao.queryphase.model._
import com.googlecode.mapperdao.schema.Type
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.queryphase.model.InQueryTable
import com.googlecode.mapperdao.queryphase.model.Select
import com.googlecode.mapperdao.queryphase.model.Join
import com.googlecode.mapperdao.queryphase.model.From
import com.googlecode.mapperdao.queryphase.model.OnClause
import com.googlecode.mapperdao.schema.ManyToMany
import com.googlecode.mapperdao.queryphase.model.Column

/**
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
class QueryPhase
{
	private val alreadyDone = collection.mutable.Set.empty[Type[_, _]]

	def toQuery[ID, PC <: Persisted, T](q: QueryBuilder[ID, PC, T]): Select = {
		val tpe = q.entity.tpe
		val iqt = InQueryTable(Table(tpe.table), "maint")
		val from = From(iqt)
		val entityJoins = joins(tpe, iqt)
		val w = where(tpe, iqt, q)
		Select(from, entityJoins, w)
	}

	private def joins[ID, T](tpe: Type[ID, T], iqt: InQueryTable): List[Join] = {
		// make sure we don't process the same type twice
		if (alreadyDone.contains(tpe))
			Nil
		else {
			alreadyDone += tpe
			tpe.table.relationshipColumns.map {
				case ManyToMany(e, linkTable, foreign) =>
					val linkT = Table(linkTable)
					val linkIQT = InQueryTable(linkT, alias)
					val ftpe = foreign.entity.tpe
					val rightT = Table(ftpe.table)
					val rightIQT = InQueryTable(rightT, alias)
					Join(
						linkIQT,
						OnClause(
							tpe.table.primaryKeys.map {
								pk =>
									Column(iqt, pk.name)
							},
							linkTable.left.map {
								c =>
									Column(linkIQT, c.name)
							}
						)
					) :: Join(
						rightIQT,
						OnClause(
							ftpe.table.primaryKeys.map {
								pk =>
									Column(rightIQT, pk.name)
							},
							linkTable.right.map {
								c =>
									Column(linkIQT, c.name)
							}
						)
					) :: joins(ftpe, rightIQT)
			}.flatten
		}
	}

	private def where[ID, PC <: Persisted, T](mainTpe: Type[ID, T], iqt: InQueryTable, q: QueryBuilder[ID, PC, T]): Clause =
		q match {
			case wc: Query.Where[ID, PC, T] =>
				where(mainTpe, iqt, wc.builder.wheres.map(_.clauses).get)
			case _ => NoClause
		}

	private def where[ID, PC <: Persisted, T](mainTpe: Type[ID, T], iqt: InQueryTable, op: OpBase): Clause = op match {
		case Operation(left, operand, right) =>
			WhereValueComparisonClause(Column(iqt, left.column.name), operand.sql, "?")
	}

	private var aliasCnt = 0

	private def alias = {
		aliasCnt += 1
		"a" + aliasCnt
	}
}
