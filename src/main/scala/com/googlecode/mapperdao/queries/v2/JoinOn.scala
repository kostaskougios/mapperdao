package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{OrOp, AndOp, OpBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 27/09/13
 */
case class JoinOn[ID, PC <: Persisted, T](queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, PC, T] with WithWhere[ID, PC, T]
{
	def apply(op: OpBase) = {
		if (queryInfo.wheres.isDefined) throw new IllegalStateException("already defined a where clause, use and() or or()")
		JoinOn(queryInfo = queryInfo.copy(joins = Some(op)))
	}

	def and(op: OpBase) =
		Where(queryInfo = queryInfo.copy(wheres = Some(AndOp(queryInfo.wheres.get, op))))

	def or(op: OpBase) =
		Where(queryInfo = queryInfo.copy(wheres = Some(OrOp(queryInfo.wheres.get, op))))

}
