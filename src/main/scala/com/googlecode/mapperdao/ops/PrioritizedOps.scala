package com.googlecode.mapperdao.ops

import com.googlecode.mapperdao.DeclaredIds

/**
 * @author kostantinos.kougios
 *
 * 21 Nov 2012
 */
class PrioritizedOps {

}

case class OpAndPriority[ID, PC <: DeclaredIds[ID], T](
	op: PersistOp[ID, PC, T],
	priority: Int)