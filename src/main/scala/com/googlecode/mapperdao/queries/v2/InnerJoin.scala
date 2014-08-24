package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.OpBase
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

import scala.language.{existentials, implicitConversions}

/**
 * @author kostas.kougios
 *         Date: 10/09/13
 */
case class InnerJoin[JID, JT, FID, FT](
	joinEntity: Alias[JID, JT],
	ci: ColumnInfoRelationshipBase[JT, _, FID, FT],
	foreignEntity: Alias[FID, FT],
	ons: Option[OpBase] = None
	) extends Join
