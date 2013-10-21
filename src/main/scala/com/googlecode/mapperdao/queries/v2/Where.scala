package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{OpBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 11/09/13
 */
class Where[ID, PC <: Persisted, T](private[mapperdao] val queryInfo: QueryInfo[ID, T])
	extends WithQueryInfo[ID, PC, T]
	with WhereBooleanOps[ID, PC, T]
	with WithOrderBy[ID, PC, T]
{
	def apply(op: OpBase) = {
		if (queryInfo.wheres.isDefined) throw new IllegalStateException("already defined a where clause, use and() or or()")
		new Where[ID, PC, T](queryInfo = queryInfo.copy(wheres = Some(op)))
	}
}