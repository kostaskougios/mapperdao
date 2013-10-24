package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.OpBase
import com.googlecode.mapperdao.Query.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class QueryInfo[ID, T](
	entityAlias: Alias[ID, T],
	wheres: Option[OpBase] = None,
	joins: List[Join] = Nil,
	order: List[(AliasColumn[_], AscDesc)] = Nil
	)
