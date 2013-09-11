package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class AfterFrom[ID, PC <: Persisted, T](queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, T]
{
	def where = Where[ID, PC, T](queryInfo)
}
