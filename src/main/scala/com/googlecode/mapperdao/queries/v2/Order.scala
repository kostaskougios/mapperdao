package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.queries.v2.Query2.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 15/10/13
 */
class Order[ID, PC <: Persisted, T](private val qi: QueryInfo[ID, T])
{
	def apply(obs: List[(AliasColumn[_], AscDesc)]): WithQueryInfo[ID, PC, T] = new WithQueryInfo[ID, PC, T]
	{
		val queryInfo = qi.copy(order = obs ::: qi.order)
	}
}
