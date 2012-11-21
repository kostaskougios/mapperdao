package com.googlecode.mapperdao.ops

import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.Entity

/**
 * entities are converted to PersistOps
 *
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
class PersistOperationsManager {
	def toInsertOps[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		o: T,
		valuesMap: ValuesMap): PersistOp = {

	}
}