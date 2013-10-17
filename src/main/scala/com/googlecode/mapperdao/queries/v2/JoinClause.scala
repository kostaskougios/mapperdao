package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 12/09/13
 */
case class JoinClause[ID, PC <: Persisted, T, FID, FT](
	queryInfo: QueryInfo[ID, T],
	to: Alias[FID, FT]
	) extends WithQueryInfo[ID, PC, T]
{
	def where = new Where[ID, PC, T](queryInfo)
}