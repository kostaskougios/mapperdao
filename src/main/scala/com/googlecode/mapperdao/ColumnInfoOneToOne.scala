package com.googlecode.mapperdao

case class ColumnInfoOneToOne[T, FID, F](
	val column: OneToOne[FID, F],
	val columnToValue: (_ >: T) => F
)
	extends ColumnInfoRelationshipBase[T, F, FID, F] {
	override val getterMethod = None
}
