package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase

/**
 * @author: kostas.kougios
 *          Date: 13/09/13
 */
case class Alias[ID, T](entity: EntityBase[ID, T], tableAlias: Option[Symbol] = None)

object Alias
{
	def apply[ID, T](e: EntityBase[ID, T], tableAlias: Symbol): Alias[ID, T] = apply(e, Some(tableAlias))
}