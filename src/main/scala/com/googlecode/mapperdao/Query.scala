package com.googlecode.mapperdao

import com.googlecode.mapperdao.queries._
import com.googlecode.mapperdao.queries.v2._
import com.googlecode.mapperdao.schema.ColumnInfo
import scala.Some
import com.googlecode.mapperdao.queries.v2.AliasColumn

/**
 * query builder and DSL
 *
 * typical usage is to
 *
 * import Query._
 *
 * val pe=ProductEntity
 * val jeans=(select
 * from pe
 * where pe.title==="jeans").toList
 *
 * The import makes sure the implicits and builders for the DSL can be used.
 * All classes of this object are internal API of mapperdao and can not be
 * used externally.
 *
 * Compilation errors sometimes can be tricky to understand but this is common
 * with DSL's. Please read the examples on the wiki pages or go through the
 * mapperdao examples / test suites.
 *
 * @author kostantinos.kougios
 *
 *         15 Aug 2011
 */
object Query
	extends SqlRelationshipImplicitConvertions
	with SqlOneToOneImplicitConvertions
	with SqlManyToOneImplicitConvertions
	with SqlManyToManyImplicitConvertions
	with SqlOneToManyImplicitConvertions
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
			def apply[ID, PC <: Persisted, T](column: AliasColumn[_]): List[(AliasColumn[_], AscDesc)] =
				List((column, asc))

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
