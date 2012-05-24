package com.googlecode.mapperdao

import java.util.IdentityHashMap

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * contains entities sorted via 2 keys: class and ids
 *
 * @author kostantinos.kougios
 *
 * 7 Aug 2011
 */
private[mapperdao] trait EntityMap {

	def putMock[T](clz: Class[_], ids: List[Any], entity: T): Unit
	def get[T](clz: Class[_], ids: List[Any])(f: => Option[T]): Option[T]
	def down[PC, T, V, FPC, F](o: Type[PC, T], ci: ColumnInfoRelationshipBase[T, V, FPC, F], dv: DatabaseValues): Unit
	def peek[PC, T, V, FPC, F]: SelectInfo[PC, T, V, FPC, F]
	def up: Unit
}

protected case class SelectInfo[PC, T, V, FPC, F](
	val tpe: Type[PC, T],
	val ci: ColumnInfoRelationshipBase[T, V, FPC, F],
	val databaseValues: DatabaseValues)
