package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * @author: kostas.kougios
 *          Date: 18/10/13
 */
trait WithJoin[ID, PC <: Persisted, T]
{
	private[mapperdao] val queryInfo: QueryInfo[ID, T]

	def join[JID, JT, FID, FT](from: Alias[JID, JT], column: ColumnInfoRelationshipBase[JT, _, FID, FT], to: Alias[FID, FT]) =
		JoinClause[ID, PC, T, FID, FT](queryInfo.copy(
			joins = InnerJoin(from, column, to) :: queryInfo.joins
		), to)

	def join[FID, FT](e: Alias[FID, FT]) = SelfJoinOn[ID, PC, T, FID, FT](queryInfo.copy(
		joins = SelfJoin(e) :: queryInfo.joins
	), e)

}
