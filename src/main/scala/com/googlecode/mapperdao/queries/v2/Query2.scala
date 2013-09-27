package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, Persisted}
import com.googlecode.mapperdao.queries.SqlImplicitConvertions

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
object Query2 extends SqlImplicitConvertions
{
	def select[ID, PC <: Persisted, T] = new From[ID, PC, T]

	implicit def implicitAs[ID, T](e: EntityBase[ID, T]) = new
		{
			def as[ID, T](symbol: Symbol) = Alias(e, Some(symbol))
		}
}
