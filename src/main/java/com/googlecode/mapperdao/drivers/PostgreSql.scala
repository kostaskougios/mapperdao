package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
class PostgreSql(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

	private val invalidColumnNames = Set("end", "select", "where", "group")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name

	override def toString = "PostgreSql"
}