package com.googlecode.mapperdao

import java.util.IdentityHashMap

import scala.collection.immutable.Stack
import scala.collection.mutable.HashMap

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * contains entities sorted via 2 keys: class and ids
 *
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
private[mapperdao] class EntityMap(
		m: scala.collection.mutable.Map[List[Any], Option[_]] = HashMap(),
		test: Boolean = true) {
	private var stack = Stack[SelectInfo[_, _, _, _, _]]()

	private def key(clz: Class[_], ids: List[Any]) = clz :: ids

	def put[T](clz: Class[_], ids: List[Any], entity: T): Unit =
		{
			val k = key(clz, ids)
			if (test && m.contains(k)) throw new IllegalStateException("ids %s already contained for %s".format(ids, clz))
			m(k) = Some(entity)
		}

	def reput[T](clz: Class[_], ids: List[Any], entity: T): Unit =
		{
			val k = key(clz, ids)
			m(k) = Some(entity)
		}

	def get[T](clz: Class[_], ids: List[Any])(f: => Option[T]): Option[T] = {
		val k = key(clz, ids)
		m.getOrElse(k, {
			val vo = f
			m(k) = vo
			vo
		}).asInstanceOf[Option[T]]
	}

	def down[PC, T, V, FPC, F](o: Type[PC, T], ci: ColumnInfoRelationshipBase[T, V, FPC, F], jdbcMap: JdbcMap): Unit =
		stack = stack.push(SelectInfo(o, ci, jdbcMap))

	def peek[PC, T, V, FPC, F] = (if (stack.isEmpty) SelectInfo(null, null, null) else stack.top).asInstanceOf[SelectInfo[PC, T, V, FPC, F]]

	def up = stack = stack.pop

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	override def toString = "EntityMap(%s)".format(m.toString)
}
protected case class SelectInfo[PC, T, V, FPC, F](val tpe: Type[PC, T], val ci: ColumnInfoRelationshipBase[T, V, FPC, F], val jdbcMap: JdbcMap)
