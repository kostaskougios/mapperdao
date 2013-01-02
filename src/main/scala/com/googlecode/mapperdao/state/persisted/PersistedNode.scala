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
	val tpe: Type[ID, T]
	val mainEntity: Boolean

	def identity: Int
}
