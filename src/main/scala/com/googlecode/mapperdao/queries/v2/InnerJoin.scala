package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.EntityBase
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class InnerJoin[JID, JT, FID, FT](
	joinEntity: EntityBase[JID, JT],
	ci: ColumnInfoRelationshipBase[JT, _, FID, FT],
	foreignEntity: EntityBase[FID, FT]
	) extends Join
