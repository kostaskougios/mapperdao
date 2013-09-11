package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{AndOp, OpBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 11/09/13
 */
case class Where[ID, PC <: Persisted, T](queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, T]
{
	def apply(op: OpBase) = {
		if (queryInfo.wheres.isDefined) throw new IllegalStateException("already defined a where clause, use and() or or()")
		Where(queryInfo = queryInfo.copy(wheres = Some(op)))
	}

	def and(op: OpBase) =
		Where(queryInfo = queryInfo.copy(wheres = Some(AndOp(queryInfo.wheres.get, op))))
}