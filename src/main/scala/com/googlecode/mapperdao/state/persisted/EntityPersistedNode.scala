package com.googlecode.mapperdao.state.persisted

import com.googlecode.mapperdao._

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
case class EntityPersistedNode[ID, T](
	tpe: Type[ID, T],
	oldVM: Option[ValuesMap],
	newVM: ValuesMap,
	mainEntity: Boolean
	) extends PersistedNode[ID, T]
{
	val identity = newVM.identity

	override def toString = "EntityPersistedNode(" + tpe + ")"
}
