package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * @author: kostas.kougios
 *          Date: 13/09/13
 */
case class SelfJoinOn[ID, PC <: Persisted, T, FID, FT](queryInfo: QueryInfo[ID, T], e: Alias[FID, FT])
{
	def on = Where[ID, PC, T](queryInfo)
}