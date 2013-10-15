package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.queries.v2.Query2.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 15/10/13
 */
class Order[ID, PC <: Persisted, T](val queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, PC, T]
{
	def by(column: AliasColumn[_], ascDesc: AscDesc) = {
		queryInfo.copy(order = (column, ascDesc) :: queryInfo.order)
	}
}
