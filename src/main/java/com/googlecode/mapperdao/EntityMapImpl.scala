package com.googlecode.mapperdao

import scala.collection.immutable.Stack
import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 6 May 2012
 */
private[mapperdao] class EntityMapImpl extends EntityMap {
	private val m = scala.collection.mutable.ListMap[List[Any], Option[_]]()

	protected def key(clz: Class[_], ids: List[Any]) = clz :: ids

	def putMock[T](clz: Class[_], ids: List[Any], entity: T): Unit =
		{
			val k = key(clz, ids)
			this.synchronized {
				if (m.contains(k)) {
					// mocks should only "put" if the map doesn't already have a value
					throw new IllegalStateException("ids %s already contained for %s".format(ids, clz))
				} else {
					m(k) = Some(entity)
				}
			}
		}

	def get[T](clz: Class[_], ids: List[Any])(f: => Option[T]): Option[T] = {
		val k = key(clz, ids)
		this.synchronized {
			m.getOrElse(k, {
				val vo = f
				m(k) = vo
				vo
			})
		}.asInstanceOf[Option[T]]
	}

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

	override def toString = "EntityMapImpl(%s)".format(m.toString)
}