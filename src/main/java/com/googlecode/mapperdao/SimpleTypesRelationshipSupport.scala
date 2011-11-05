package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 5 Nov 2011
 */

case class StringValue(val value: String)

class StringEntity(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[StringValue](table, classOf[StringValue]) {
	val value = string(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new StringValue(value) with Persisted
}
