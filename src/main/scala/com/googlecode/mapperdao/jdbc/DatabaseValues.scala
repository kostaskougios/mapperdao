package com.googlecode.mapperdao.jdbc

import com.googlecode.mapperdao.schema.{ColumnRelationshipBase, SimpleColumn}

/**
 * @author kostantinos.kougios
 *
 *         18 May 2012
 */
class DatabaseValues(
	map: Map[String, Any],
	relatedValues: Map[String, List[DatabaseValues]] = Map()
	)
{
	def apply(column: SimpleColumn): Any = map(column.nameLowerCase)

	def related(c: ColumnRelationshipBase[_, _]) = relatedValues.get(c.aliasLowerCase)

	def toMap: Map[String, Any] = map

	override def toString = "DatabaseValues(%s)".format(map)
}