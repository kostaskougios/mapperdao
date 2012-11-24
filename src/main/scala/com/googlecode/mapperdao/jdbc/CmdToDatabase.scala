package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.ops._
import com.googlecode.mapperdao.drivers.Driver
import org.springframework.jdbc.core.SqlParameterValue

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

	private case class SqlCmd[ID, PC <: DeclaredIds[ID], T](
		cmd: PersistCmd[ID, PC, T],
		sql: String,
		values: Array[SqlParameterValue])

	def insert[ID, PC <: DeclaredIds[ID], T](
		cmds: List[PersistCmd[ID, PC, T]]): List[T with PC] = {
		val sqlCmds = cmds.map { cmd =>
			val sql = toSql(cmd)
			SqlCmd(cmd, sql.sql, sql.values.toArray)
		}

		sqlCmds.groupBy(_.sql).map {
			case (sql, cmds) =>
				val entity = cmds.head.cmd.entity
				val table = entity.tpe.table
				val autoGeneratedColumnNames = table.autoGeneratedColumnNamesArray
				val bo = BatchOptions(driver.batchStrategy, autoGeneratedColumnNames)
				val args = cmds.map {
					case SqlCmd(_, _, values) =>
						values
				}.toArray
				jdbc.batchUpdate(bo, sql, args)
		}

		Nil
	}

	private def toSql(cmd: PersistCmd[_, _, _]) = cmd match {
		case InsertCmd(entity, o, columns) =>
			driver.insertSql(entity.tpe, columns).result
	}
}