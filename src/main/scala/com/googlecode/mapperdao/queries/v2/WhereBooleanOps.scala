package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Persisted, OrOp, AndOp, OpBase}

/**
 * @author: kostas.kougios
 *          Date: 18/10/13
 */
trait WhereBooleanOps[ID, PC <: Persisted, T]
{
	private[mapperdao] val queryInfo: QueryInfo[ID, T]

	def and(op: OpBase) =
		new Where[ID, PC, T](queryInfo = queryInfo.copy(wheres = Some(AndOp(queryInfo.wheres.get, op))))

	def or(op: OpBase) =
		new Where[ID, PC, T](queryInfo = queryInfo.copy(wheres = Some(OrOp(queryInfo.wheres.get, op))))
}
