package com.rits.orm.drivers
import com.rits.jdbc.Jdbc

/**
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
class PostgreSql(override val jdbc: Jdbc) extends Driver {

	val invalidColumnNames = Set("end", "select", "where", "group")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name

	override def toString = "PostgreSql"
}