package com.googlecode.mapperdao

import java.util.IdentityHashMap
import com.googlecode.mapperdao.jdbc.JdbcMap
import scala.collection.mutable.ListMap

/**
 * contains entities sorted via 2 keys: class and ids
 *
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
private[mapperdao] case class EntityMap(
		private val m: scala.collection.mutable.ListMap[List[Any], Option[_]] = ListMap(),
		private val parent: SelectInfo[_, _, _, _, _] = SelectInfo(null, null, null)) {
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

	def down[PC, T, V, FPC, F](selectConfig: SelectConfig, tpe: Type[PC, T], ci: ColumnInfoRelationshipBase[T, V, FPC, F], dv: DatabaseValues): EntityMap =
		if (selectConfig.lazyLoad.isLazyLoaded(ci))
			copy(m = ListMap(), parent = SelectInfo(tpe, ci, dv))
		else
			copy(parent = SelectInfo(tpe, ci, dv))

	def peek[PC, T, V, FPC, F] =
		parent.asInstanceOf[SelectInfo[PC, T, V, FPC, F]]

	override def toString = "EntityMapImpl(%s)".format(m.toString)
}

protected case class SelectInfo[PC, T, V, FPC, F](
	val tpe: Type[PC, T],
	val ci: ColumnInfoRelationshipBase[T, V, FPC, F],
	val databaseValues: DatabaseValues)
