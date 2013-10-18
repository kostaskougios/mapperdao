package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{EntityBase, Persisted}
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * @author: kostas.kougios
 *          Date: 18/10/13
 */
trait WithJoin[ID, PC <: Persisted, T]
{
	private[mapperdao] val queryInfo: QueryInfo[ID, T]

	def join[JID, JT, FID, FT](from: EntityBase[JID, JT], column: ColumnInfoRelationshipBase[JT, _, FID, FT], to: EntityBase[FID, FT]) =
		JoinClause[ID, PC, T, FID, FT](queryInfo.copy(
			joins = InnerJoin(Alias(from), column, Alias(to)) :: queryInfo.joins
		), Alias(to))

	def join[FID, FT](e: Alias[FID, FT]) = SelfJoinOn[ID, PC, T, FID, FT](queryInfo.copy(
		joins = SelfJoin(e) :: queryInfo.joins
	), e)

}
