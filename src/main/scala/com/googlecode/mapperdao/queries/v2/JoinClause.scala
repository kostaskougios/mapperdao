package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Persisted, EntityBase}

/**
 * @author: kostas.kougios
 *          Date: 12/09/13
 */
case class JoinClause[ID, PC <: Persisted, T, FID, FT](
	queryInfo: QueryInfo[ID, T],
	to: EntityBase[FID, FT]
	) extends WithQueryInfo[ID, PC, T]
{
	def where = Where(queryInfo)
}