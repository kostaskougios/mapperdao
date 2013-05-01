package com.googlecode.mapperdao.state.persisted

import com.googlecode.mapperdao._

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class ExternalEntityPersistedNode[ID, T](
	entity: ExternalEntity[ID, T],
	o: T
	) extends PersistedNode[ID, T]
{
	val tpe = entity.tpe
	val mainEntity = false
}
