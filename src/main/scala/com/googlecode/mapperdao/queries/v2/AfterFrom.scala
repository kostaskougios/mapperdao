package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
class AfterFrom[ID, PC <: Persisted, T](private[mapperdao] val queryInfo: QueryInfo[ID, T])
	extends WithQueryInfo[ID, PC, T]
	with WithWhere[ID, PC, T]
	with WithJoin[ID, PC, T]
	with WithOrderBy[ID, PC, T]
	with Execution[ID, PC, T]