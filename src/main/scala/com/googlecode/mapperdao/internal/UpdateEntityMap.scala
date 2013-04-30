package com.googlecode.mapperdao.internal

import scala.collection.immutable.Stack
import collection.mutable
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase
import com.googlecode.mapperdao.{ValuesMap, Entity, Persisted}

protected[mapperdao] class UpdateEntityMap
{
	private val m = new mutable.HashMap[ValuesMap, Any]
	private var stack = Stack[UpdateInfo[_, _, _, _, _]]()

	def put[T](vm: ValuesMap, mock: Persisted with T) {
		m.put(vm, mock)
	}

	def get[T](vm: ValuesMap): Option[Persisted with T] = {
		val g = m.get(vm).asInstanceOf[Option[Persisted with T]]
		g
	}

	def down[PID, PT, V, FID, F](
		o: PT,
		ci: ColumnInfoRelationshipBase[PT, V, FID, F],
		parentEntity: Entity[PID, Persisted, PT]
		) {
		stack = stack.push(UpdateInfo(o, ci, parentEntity))
	}

	def peek[PID, PT, V, FID, F] =
		(if (stack.isEmpty) UpdateInfo(null, null, null) else stack.top).asInstanceOf[UpdateInfo[PID, PT, V, FID, F]]

	def up() {
		stack = stack.pop
	}

	def done() {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	def toErrorStr = {
		val sb = new StringBuilder
		stack.foreach {
			u =>
				sb append u.o append ('\n')
		}
		sb.toString()
	}
}

protected[mapperdao] case class UpdateInfo[PID, PT, V, FID, F](
	o: PT,
	ci: ColumnInfoRelationshipBase[PT, V, FID, F],
	parentEntity: Entity[PID, Persisted, PT]
	)
