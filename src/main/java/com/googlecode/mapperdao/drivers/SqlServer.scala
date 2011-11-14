package com.googlecode.mapperdao.drivers

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 13 Nov 2011
 */
class SqlServer(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {
	private val invalidColumnNames = Set("end", "select", "where", "group")
	private val invalidTableNames = Set("end", "select", "where", "group", "user")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name
	override def escapeTableNames(name: String): String = if (invalidTableNames.contains(name)) '"' + name + '"'; else name
}