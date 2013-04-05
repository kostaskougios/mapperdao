package com.googlecode.mapperdao

import scala.collection.immutable.ListMap
import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author kostantinos.kougios
 *
 *         18 May 2012
 */
private[mapperdao] class DatabaseValues(val map: ListMap[String, Any])
{
	def apply(column: SimpleColumn): Any = map(column.nameLowerCase)

	def toMap: Map[String, Any] = map

	override def toString = "DatabaseValues(%s)".format(map)
}