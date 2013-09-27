package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class InnerJoin[JID, JT, FID, FT](
	joinEntity: Alias[JID, JT],
	ci: ColumnInfoRelationshipBase[JT, _, FID, FT],
	foreignEntity: Alias[FID, FT]
	) extends Join
