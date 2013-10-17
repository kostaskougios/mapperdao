package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 17/10/13
 */
class Extend[ID, PC <: Persisted, T](private[mapperdao] val queryInfo: QueryInfo[ID, T])
{

}
