package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase
import com.googlecode.mapperdao.queries.SqlImplicitConvertions

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
object Query2 extends SqlImplicitConvertions
{
	def select = new From

	implicit def implicitAs[ID, T](e: EntityBase[ID, T]) = new
		{
			def as[ID, T](symbol: Symbol) = Alias(e, Some(symbol))
		}
}
