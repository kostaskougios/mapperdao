package com.googlecode.mapperdao

/**
 * double simple type
 */
case class DoubleValue(val value: Double) extends SimpleTypeValue[Double, DoubleValue] {
	def compareTo(o: DoubleValue): Int = value.compare(o.value)
}

protected class DoubleEntityOTM(table: String, fkColumn: String, soleColumn: String)
		extends Entity[Nothing, NoId, DoubleValue](table, classOf[DoubleValue]) {
	val value = column(soleColumn) to (_.value)
	declarePrimaryKey(value)

	def constructor(implicit m: ValuesMap) = new DoubleValue(value) with NoId
}

abstract class DoubleEntityManyToManyBase[ID, PC <: DeclaredIds[ID]](table: String, soleColumn: String)
		extends Entity[ID, PC, DoubleValue](table, classOf[DoubleValue]) {
	val value = column(soleColumn) to (_.value)
}
class DoubleEntityManyToManyAutoGenerated(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None)
		extends DoubleEntityManyToManyBase[Int, SurrogateIntId](table, soleColumn) {
	val id = key(pkColumn) sequence (sequence) autogenerated (_.id)
	def constructor(implicit m: ValuesMap) = new DoubleValue(value) with Persisted with SurrogateIntId {
		val id: Int = DoubleEntityManyToManyAutoGenerated.this.id
	}
}

object DoubleEntity {
	def oneToMany(table: String, fkColumn: String, soleColumn: String) = new DoubleEntityOTM(table, fkColumn, soleColumn)
	def manyToManyAutoGeneratedPK(table: String, pkColumn: String, soleColumn: String, sequence: Option[String] = None) = new DoubleEntityManyToManyAutoGenerated(table, pkColumn, soleColumn, sequence)
}
