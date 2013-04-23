package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author kostantinos.kougios
 *
 *         18 May 2012
 */
class DatabaseValues(val map: Map[String, Any])
{
	def apply(column: SimpleColumn): Any = map(column.nameLowerCase)

	def toMap: Map[String, Any] = map

	override def toString = "DatabaseValues(%s)".format(map)
}