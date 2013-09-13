package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Query.JoinOn
import com.googlecode.mapperdao.{EntityBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class SelfJoin[JID, JT, FID, FT, QID, QPC <: Persisted, QT](
	// for join on functionality
	entity: EntityBase[JID, JT],
	on: JoinOn[QID, QPC, QT]
	) extends Join
