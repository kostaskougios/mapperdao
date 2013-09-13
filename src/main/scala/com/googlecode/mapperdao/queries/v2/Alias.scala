package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase

/**
 * @author: kostas.kougios
 *          Date: 13/09/13
 */
case class Alias[ID, T](e: EntityBase[ID, T]) extends (() => EntityBase[ID, T])
{
	def apply = e
}
