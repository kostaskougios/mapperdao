package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, Persisted}
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class AfterFrom[ID, PC <: Persisted, T](queryInfo: QueryInfo[ID, T]) extends WithQueryInfo[ID, PC, T]
{
	def where = Where[ID, PC, T](queryInfo)

	def join[JID, JT, FID, FT](from: EntityBase[JID, JT], column: ColumnInfoRelationshipBase[JT, _, FID, FT], to: EntityBase[FID, FT]) =
		JoinClause[ID, PC, T, FID, FT](queryInfo.copy(
			joins = InnerJoin(from, column, to) :: queryInfo.joins
		), to)

	def join[FID, FT](e: Alias[FID, FT]) = SelfJoinOn(queryInfo, e)
}
