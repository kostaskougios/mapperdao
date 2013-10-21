package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 21/10/13
 */
trait WithOrderBy[ID, PC <: Persisted, T]
{
	private[mapperdao] val queryInfo: QueryInfo[ID, T]

	def orderBy = new Order[ID, PC, T](queryInfo)
}
