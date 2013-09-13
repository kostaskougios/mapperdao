package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 13/09/13
 */
trait OpsLike[ID, PC <: Persisted, T] extends WithQueryInfo[ID, PC, T]
{
	val queryInfo: QueryInfo[ID, T]
}
