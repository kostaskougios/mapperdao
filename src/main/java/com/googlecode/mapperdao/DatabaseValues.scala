package com.googlecode.mapperdao

import scala.collection.immutable.ListMap

/**
 * @author kostantinos.kougios
 *
 * 18 May 2012
 */
private[mapperdao] class DatabaseValues(val map: ListMap[String, Any]) {
	def apply(columnName: String) = map(columnName.toLowerCase)
}