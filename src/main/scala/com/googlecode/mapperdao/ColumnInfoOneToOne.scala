package com.googlecode.mapperdao

case class ColumnInfoOneToOne[T, FPC, F](
	val column: OneToOne[FPC, F],
	val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F] {
	override val getterMethod = None
}
