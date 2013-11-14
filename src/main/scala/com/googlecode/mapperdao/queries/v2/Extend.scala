package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * extends a query so that more operations can be added
 *
 * @author: kostas.kougios
 *          Date: 17/10/13
 */
class Extend[ID, PC <: Persisted, T](private[mapperdao] val queryInfo: QueryInfo[ID, T])
	extends WhereBooleanOps[ID, PC, T]
	with WithJoin[ID, PC, T]
	with WithOrderBy[ID, PC, T]