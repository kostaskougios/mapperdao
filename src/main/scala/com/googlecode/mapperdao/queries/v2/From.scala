package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
class From[ID, PC <: Persisted, T]
{
	def from(entity: EntityBase[ID, T]) = AfterFrom(QueryInfo(entity))
}

