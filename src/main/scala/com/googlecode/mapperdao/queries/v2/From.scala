package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Entity, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
class From
{
	def from[ID, PC <: Persisted, T](entity: Entity[ID, PC, T]) = AfterFrom[ID, PC, T](QueryInfo(Alias(entity, Some('maint))))
}

