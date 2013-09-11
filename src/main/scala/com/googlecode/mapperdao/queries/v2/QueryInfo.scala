package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, OpBase}
import com.googlecode.mapperdao.schema.ColumnInfo
import com.googlecode.mapperdao.Query.AscDesc

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class QueryInfo[ID, T](
	entity: EntityBase[ID, T],
	wheres: Option[OpBase] = None,
	joins: List[Join] = Nil,
	order: List[(ColumnInfo[_, _], AscDesc)] = Nil
	)
