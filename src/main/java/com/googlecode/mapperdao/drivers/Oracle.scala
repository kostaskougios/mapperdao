package com.googlecode.mapperdao.drivers

import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 23 Sep 2011
 */
class Oracle(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {
	private val invalidColumnNames = Set("select", "where", "group", "start")

	override def escapeColumnNames(name: String) = if (invalidColumnNames.contains(name)) '"' + name + '"'; else name
}