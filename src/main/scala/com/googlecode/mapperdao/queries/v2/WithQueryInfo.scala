package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted

/**
 * this is created as a result of a query, please see
 *
 * https://code.google.com/p/mapperdao/wiki/Queries
 *
 * @example {{{
 *           	// import the query dsl
 *           	import Query._
 *
 *           	// and then write a query
 *           	val q=select from MyEntity where MyEntity.name='a name'
 *          }}}
 *
 * @author kostas.kougios
 *          Date: 11/09/13
 */
trait WithQueryInfo[ID, PC <: Persisted, T]
{
	private[mapperdao] def queryInfo: QueryInfo[ID, T]
}
