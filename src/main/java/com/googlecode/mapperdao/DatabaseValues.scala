package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 18 May 2012
 */
class DatabaseValues(val map: Map[String, Any]) {
	def apply(columnName: String) = map(columnName.toLowerCase)
}