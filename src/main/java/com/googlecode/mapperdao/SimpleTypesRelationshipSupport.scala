package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 5 Nov 2011
 */

trait SimpleTypeValue[T, E] extends Comparable[E] {
	val value: T
}

/**
 * string simple type
 */
case class StringValue(val value: String) extends SimpleTypeValue[String, StringValue] {
	def compareTo(o: StringValue): Int = value.compareTo(o.value)
}

class StringEntity(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[StringValue](table, classOf[StringValue]) {
	val value = string(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new StringValue(value) with Persisted
}

/**
 * int simple type
 */
case class IntValue(val value: Int) extends SimpleTypeValue[Int, IntValue] {
	def compareTo(o: IntValue): Int = value - o.value
}

class IntEntity(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[IntValue](table, classOf[IntValue]) {
	val value = int(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new IntValue(value) with Persisted
}
