package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{Persisted, EntityBase}
import com.googlecode.mapperdao.schema.ColumnInfo

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
object Query2
{
	def select = new From

	def extend[ID, PC <: Persisted, T](wqi: WithQueryInfo[ID, PC, T]) = new Extend[ID, PC, T](wqi.queryInfo)

	implicit def implicitAs[ID, T](e: EntityBase[ID, T]) = new
		{
			def as[ID, T](symbol: Symbol) = Alias(e, Some(symbol))
		}

	implicit def columnToAlias[V](v: ColumnInfo[_, V]) = new AliasColumn[V](v.column)

	implicit def columnToAlias[V](v: (Symbol, ColumnInfo[_, V])) = new AliasColumn[V](v._2.column, Some(v._1))

	sealed abstract class AscDesc
	{
		val sql: String
	}

	object asc extends AscDesc
	{
		val sql = "asc"
	}

	object desc extends AscDesc
	{
		val sql = "desc"
	}

	// used on "order by" clauses
	val by = new
		{
			def apply[ID, PC <: Persisted, T](column: AliasColumn[_], ascDesc: AscDesc): List[(AliasColumn[_], AscDesc)] =
				List((column, ascDesc))

			def apply[ID, PC <: Persisted, T](
				column1: AliasColumn[_],
				ascDesc1: AscDesc,
				column2: AliasColumn[_],
				ascDesc2: AscDesc
				): List[(AliasColumn[_], AscDesc)] = (column1, ascDesc1) ::(column2, ascDesc2) :: Nil

			def apply[ID, PC <: Persisted, T](obs: List[(AliasColumn[_], AscDesc)]): List[(AliasColumn[_], AscDesc)] = obs
		}
}
