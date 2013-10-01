package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{OrOp, AndOp, OpBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 27/09/13
 */
case class JoinOn[ID, PC <: Persisted, T](queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, PC, T] with WithWhere[ID, PC, T]
{
	private def joinList(op: OpBase) = {
		val j = queryInfo.joins.head match {
			case sj: SelfJoin[_, _, _, _, _, _, _] =>
				sj.copy(ons = Some(op))
		}
		j :: queryInfo.joins.tail
	}

	def apply(op: OpBase) = {
		if (queryInfo.wheres.isDefined) throw new IllegalStateException("already defined a where clause, use and() or or()")

		JoinOn(queryInfo = queryInfo.copy(joins = joinList(op)))
	}

	def and(op: OpBase) =
		JoinOn(queryInfo = queryInfo.copy(joins = joinList(AndOp(queryInfo.joins.head.ons.get, op))))

	def or(op: OpBase) =
		JoinOn(queryInfo = queryInfo.copy(joins = joinList(OrOp(queryInfo.joins.head.ons.get, op))))

}
