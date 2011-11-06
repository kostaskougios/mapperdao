package com.googlecode.mapperdao

/**
 * mapping simple type values to tables. These classes provide easy integration with
 * tables holding 1 simple type.
 *
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

protected class StringEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[StringValue](table, classOf[StringValue]) {
	val value = string(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new StringValue(value) with Persisted
}

object StringEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new StringEntityOTM(table, fkColumn, soleColumn)
}

/**
 * int simple type
 */
case class IntValue(val value: Int) extends SimpleTypeValue[Int, IntValue] {
	def compareTo(o: IntValue): Int = value.compareTo(o.value)
}

protected class IntEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[IntValue](table, classOf[IntValue]) {
	val value = int(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new IntValue(value) with Persisted
}

object IntEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new IntEntityOTM(table, fkColumn, soleColumn)
}
/**
 * long simple type
 */
case class LongValue(val value: Long) extends SimpleTypeValue[Long, LongValue] {
	def compareTo(o: LongValue): Int = value.compare(o.value)
}

protected class LongEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[LongValue](table, classOf[LongValue]) {
	val value = long(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new LongValue(value) with Persisted
}

object LongEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new LongEntityOTM(table, fkColumn, soleColumn)
}

/**
 * float simple type
 */
case class FloatValue(val value: Float) extends SimpleTypeValue[Float, FloatValue] {
	def compareTo(o: FloatValue): Int = value.compare(o.value)
}

protected class FloatEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[FloatValue](table, classOf[FloatValue]) {
	val value = float(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new FloatValue(value) with Persisted
}

object FloatEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new FloatEntityOTM(table, fkColumn, soleColumn)
}

/**
 * double simple type
 */
case class DoubleValue(val value: Double) extends SimpleTypeValue[Double, DoubleValue] {
	def compareTo(o: DoubleValue): Int = value.compare(o.value)
}

protected class DoubleEntityOTM(table: String, fkColumn: String, soleColumn: String) extends SimpleEntity[DoubleValue](table, classOf[DoubleValue]) {
	val value = double(soleColumn, _.value)
	declarePrimaryKeys(fkColumn, soleColumn)
	def constructor(implicit m: ValuesMap) = new DoubleValue(value) with Persisted
}

object DoubleEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new DoubleEntityOTM(table, fkColumn, soleColumn)
}
