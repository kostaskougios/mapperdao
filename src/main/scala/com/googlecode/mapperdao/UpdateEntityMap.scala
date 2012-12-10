package com.googlecode.mapperdao

import scala.collection.immutable.Stack
import java.util.IdentityHashMap

protected class UpdateEntityMap {
	private val m = new IdentityHashMap[Int, Any]
	private var stack = Stack[UpdateInfo[_, _, _, _, _, _, _]]()

	def put[PC <: DeclaredIds[_], T](identity: Int, mock: PC with T with Persisted): Unit = m.put(identity, mock)
	def get[PC <: DeclaredIds[_], T](identity: Int): Option[PC with T with Persisted] =
		{
			val g = m.get(identity)
			if (g == null) None else Option(g.asInstanceOf[PC with T with Persisted])
		}

	def down[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F](
		vm: ValuesMap,
		ci: ColumnInfoRelationshipBase[PT, V, FID, FPC, F],
		parentEntity: Entity[PID, PPC, PT]): Unit =
		stack = stack.push(UpdateInfo(vm, ci, parentEntity))

	def peek[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F] =
		(if (stack.isEmpty) UpdateInfo(null, null, null) else stack.top).asInstanceOf[UpdateInfo[PID, PPC, PT, V, FID, FPC, F]]

	def up = stack = stack.pop

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	def toErrorStr = {
		val sb = new StringBuilder
		stack.foreach { u =>
			sb append u.vm append ('\n')
		}
		sb.toString
	}
}

protected case class UpdateInfo[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F](
	val vm: ValuesMap,
	val ci: ColumnInfoRelationshipBase[PT, V, FID, FPC, F],
	parentEntity: Entity[PID, PPC, PT])