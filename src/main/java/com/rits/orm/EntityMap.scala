package com.rits.orm

import scala.collection.mutable.HashMap
import java.util.IdentityHashMap
import scala.collection.immutable.Stack
import com.rits.jdbc.JdbcMap

/**
 * contains entities sorted via 2 keys: class and ids
 *
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
protected class EntityMap {
	type InnerMap = HashMap[List[Any], AnyRef]
	private val m = HashMap[Class[_], InnerMap]()
	private var stack = Stack[SelectInfo[_, _, _, _]]()

	def put[T](clz: Class[_], ids: List[Any], entity: T): Unit =
		{
			val im = m.getOrElseUpdate(clz, HashMap())
			if (im.contains(ids)) throw new IllegalStateException("ids %s already contained for %s".format(ids, clz))
			im(ids) = entity.asInstanceOf[AnyRef]
		}

	def reput[T](clz: Class[_], ids: List[Any], entity: T): Unit =
		{
			val im = m.getOrElseUpdate(clz, HashMap())
			im(ids) = entity.asInstanceOf[AnyRef]
		}

	def get[T](clz: Class[_], ids: List[Any]): Option[T] = {
		val im = m.get(clz)
		if (im.isDefined) im.get.get(ids).asInstanceOf[Option[T]] else None
	}

	def down[PC, T, V, F](o: Type[PC, T], ci: ColumnInfoRelationshipBase[T, V, F], jdbcMap: JdbcMap): Unit =
		{
			stack = stack.push(SelectInfo(o, ci, jdbcMap))
		}

	def peek[PC, T, V, F] = (if (stack.isEmpty) SelectInfo(null, null, null) else stack.top).asInstanceOf[SelectInfo[PC, T, V, F]]

	def up = stack = stack.pop

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}

	override def toString = "EntityMap(%s)".format(m.toString)
}
protected case class SelectInfo[PC, T, V, F](val tpe: Type[PC, T], val ci: ColumnInfoRelationshipBase[T, V, F], val jdbcMap: JdbcMap)

protected class UpdateEntityMap {
	private val m = new IdentityHashMap[Any, Any]
	private var stack = Stack[UpdateInfo[_, _, _]]()

	def put[PC, T](v: T, mock: PC with T): Unit = m.put(v, mock)
	def get[PC, T](v: T): Option[PC with T] =
		{
			val g = m.get(v)
			if (g == null) None else Option(g.asInstanceOf[PC with T])
		}

	def down[T, V, F](o: T, ci: ColumnInfoRelationshipBase[T, V, F]): Unit =
		{
			stack = stack.push(UpdateInfo(o, ci))
		}

	def peek[T, V, F] = (if (stack.isEmpty) UpdateInfo(null, null) else stack.top).asInstanceOf[UpdateInfo[T, V, F]]

	def up = stack = stack.pop

	def done {
		if (!stack.isEmpty) throw new InternalError("stack should be empty but is " + stack)
	}
}

protected case class UpdateInfo[T, V, F](val o: T, val ci: ColumnInfoRelationshipBase[T, V, F])