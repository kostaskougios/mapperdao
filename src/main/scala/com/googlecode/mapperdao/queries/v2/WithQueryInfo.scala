package com.googlecode.mapperdao.queries.v2

/**
 * @author: kostas.kougios
 *          Date: 11/09/13
 */
trait WithQueryInfo[ID, T]
{
	def queryInfo: QueryInfo[ID, T]
}
