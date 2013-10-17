package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 11/09/13
 */
trait WithQueryInfo[ID, PC <: Persisted, T]
{
	private[mapperdao] def queryInfo: QueryInfo[ID, T]
}
