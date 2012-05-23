package com.googlecode.mapperdao

import scala.collection.immutable.Stack
import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
trait EntityStack {
	private var stack = Stack[SelectInfo[_, _, _, _, _]]()
	def down[PC, T, V, FPC, F](o: Type[PC, T], ci: ColumnInfoRelationshipBase[T, V, FPC, F], dv: DatabaseValues): Unit =
		this.synchronized {
			stack = stack.push(SelectInfo(o, ci, dv))
		}

	def peek[PC, T, V, FPC, F] = this.synchronized {
		(if (stack.isEmpty) SelectInfo(null, null, null) else stack.top).asInstanceOf[SelectInfo[PC, T, V, FPC, F]]
	}

	def up = this.synchronized {
		stack = stack.pop
	}

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}
}