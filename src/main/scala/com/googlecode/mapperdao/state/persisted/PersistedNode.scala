package com.googlecode.mapperdao.state.persisted

import com.googlecode.mapperdao._

/**
 * after persisting to the storage, all commands are converted to persisted nodes
 *
 * @author kostantinos.kougios
 *
 *         Dec 8, 2012
 */
trait PersistedNode[ID, T] {
	val entity: Entity[ID, DeclaredIds[ID], T]
	val mainEntity: Boolean

	def identity: Int
}

case class EntityPersistedNode[ID, T](
	entity: Entity[ID, DeclaredIds[ID], T],
	oldVM: Option[ValuesMap],
	newVM: ValuesMap,
	mainEntity: Boolean
) extends PersistedNode[ID, T] {
	val identity = newVM.identity
}

case class ExternalEntityPersistedNode[ID, T](
	entity: ExternalEntity[ID, T],
	o: T,
	mainEntity: Boolean
) extends PersistedNode[ID, T] {
	val identity = System.identityHashCode(o)
}
