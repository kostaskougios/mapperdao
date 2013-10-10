package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase
import com.googlecode.mapperdao.schema.ColumnInfo

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
object Query2
{
	def select = new From

	implicit def implicitAs[ID, T](e: EntityBase[ID, T]) = new
		{
			def as[ID, T](symbol: Symbol) = Alias(e, Some(symbol))
		}

	implicit def columnToAlias[V](v: ColumnInfo[_, V]) = new AliasColumn[V](v.column)

	implicit def columnToAlias[V](v: (Symbol, ColumnInfo[_, V])) = new AliasColumn[V](v._2.column, Some(v._1))
}
