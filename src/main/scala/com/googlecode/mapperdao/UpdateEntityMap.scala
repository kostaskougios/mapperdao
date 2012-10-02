package com.googlecode.mapperdao

import java.util.IdentityHashMap

import scala.collection.immutable.Stack

protected class UpdateEntityMap {
	private val m = new IdentityHashMap[Any, Any]
	private var stack = Stack[UpdateInfo[_, _, _, _, _, _, _]]()

	def put[PC <: DeclaredIds[_], T](v: T, mock: PC with T with Persisted): Unit = m.put(v, mock)
	def get[PC <: DeclaredIds[_], T](v: T): Option[PC with T with Persisted] =
		{
			val g = m.get(v)
			if (g == null) None else Option(g.asInstanceOf[PC with T with Persisted])
		}

	def down[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F](
		o: PT,
		ci: ColumnInfoRelationshipBase[PT, V, FID, FPC, F],
		parentEntity: Entity[PID, PPC, PT]): Unit =
		stack = stack.push(UpdateInfo(o, ci, parentEntity))

	def peek[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F] =
		(if (stack.isEmpty) UpdateInfo(null, null, null) else stack.top).asInstanceOf[UpdateInfo[PID, PPC, PT, V, FID, FPC, F]]

	def up = stack = stack.pop

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	def toErrorStr = {
		val sb = new StringBuilder
		stack.foreach { u =>
			sb append u.o append ('\n')
		}
		sb.toString
	}
}

protected case class UpdateInfo[PID, PPC <: DeclaredIds[PID], PT, V, FID, FPC <: DeclaredIds[FID], F](
	val o: PT,
	val ci: ColumnInfoRelationshipBase[PT, V, FID, FPC, F],
	parentEntity: Entity[PID, PPC, PT])