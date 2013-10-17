package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted


/**
 * @author: kostas.kougios
 *          Date: 27/09/13
 */
trait WithWhere[ID, PC <: Persisted, T] extends WithQueryInfo[ID, PC, T]
{
	def where = new Where[ID, PC, T](queryInfo)
}
