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
		cmd: PersistCmd[ID, PC, T]): T with PC = {
		null.asInstanceOf[T with PC]
	}

	private def collectSqls(cmd: PersistCmd[_, _, _]) = cmd match {
		case InsertCmd(entity, o, priority, columns) =>
			driver.insertSql(entity.tpe, columns).result
	}
}