package com.googlecode.mapperdao.updatephase.persisted

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.Type

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
	val o = newVM.o

	override def toString = "EntityPersistedNode(" + tpe + ")"
}
