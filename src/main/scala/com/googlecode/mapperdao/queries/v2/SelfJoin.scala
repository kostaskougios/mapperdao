package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.Query.JoinOn
import com.googlecode.mapperdao.{Entity, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class SelfJoin[JID, JT, FID, FT, QID, QPC <: Persisted, QT](
	// for join on functionality
	entity: Entity[JID, Persisted, JT],
	on: JoinOn[QID, QPC, QT]
	) extends Join
