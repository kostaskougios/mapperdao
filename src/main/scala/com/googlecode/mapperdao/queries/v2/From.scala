package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Entity, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
class From
{

	//note:implicit alias makes scala compiler crash
	def from[ID, PC <: Persisted, T](entity: Entity[ID, PC, T]) =
		new AfterFrom[ID, PC, T](QueryInfo(Alias(entity)))

	def from[ID, PC <: Persisted, T](alias: Alias[ID, T]) =
		new AfterFrom[ID, PC, T](QueryInfo(alias))
}

