package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.ops._
import com.googlecode.mapperdao.drivers.Driver

/**
 * converts commands to database operations, executes
 * them and returns the resulting entity
 *
 * @author kostantinos.kougios
 *
 * 22 Nov 2012
 */
class CmdToDatabase(driver: Driver) {
	private val jdbc = driver.jdbc

	def insert[ID, PC <: DeclaredIds[ID], T](
		cmd: PersistCmd[ID, PC, T]): T with PC = cmd match {
		case InsertCmd(entity, o, priority, columns) =>
			val r = driver.insertSql(entity.tpe, columns).result
			null.asInstanceOf[T with PC]
	}
}