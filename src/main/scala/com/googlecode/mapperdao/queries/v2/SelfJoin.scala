package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.{OpBase, Persisted}

/**
 * @author: kostas.kougios
 *          Date: 10/09/13
 */
case class SelfJoin[JID, JT, FID, FT, QID, QPC <: Persisted, QT](
	// for join on functionality
	entity: Alias[JID, JT],
	override val ons: Option[OpBase] = None
	) extends Join
