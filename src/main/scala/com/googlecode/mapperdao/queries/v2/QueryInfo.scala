package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.OpBase
import com.googlecode.mapperdao.queries.v2.Query2.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class QueryInfo[ID, T](
	entity: Alias[ID, T],
	wheres: Option[OpBase] = None,
	joins: List[Join] = Nil,
	order: List[(AliasColumn[_], AscDesc)] = Nil
	)
