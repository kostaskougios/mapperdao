package com.rits.orm

import scala.collection.mutable.HashMap
import java.util.IdentityHashMap
import scala.collection.immutable.Stack

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

	override def toString = "EntityMap(%s)".format(m.toString)
}

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

	def peek = stack.top
	def up = stack.pop

}

protected case class UpdateInfo[T, V, F](val o: T, val ci: ColumnInfoRelationshipBase[T, V, F])