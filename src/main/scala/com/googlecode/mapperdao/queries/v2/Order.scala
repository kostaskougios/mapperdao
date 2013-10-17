package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.queries.v2.Query2.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 15/10/13
 */
class Order[ID, PC <: Persisted, T](private val qi: QueryInfo[ID, T])
{
	def apply(column: AliasColumn[_], ascDesc: AscDesc): WithQueryInfo[ID, PC, T] = apply((column, ascDesc) :: Nil)

	def apply(
		column1: AliasColumn[_],
		ascDesc1: AscDesc,
		column2: AliasColumn[_],
		ascDesc2: AscDesc
		): WithQueryInfo[ID, PC, T] = apply((column1, ascDesc1) ::(column2, ascDesc2) :: Nil)

	def apply(obs: List[(AliasColumn[_], AscDesc)]): WithQueryInfo[ID, PC, T] = new WithQueryInfo[ID, PC, T]
	{
		val queryInfo = qi.copy(order = obs ::: qi.order)
	}
}
