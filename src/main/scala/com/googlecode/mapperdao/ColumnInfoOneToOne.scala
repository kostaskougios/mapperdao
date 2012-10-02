package com.googlecode.mapperdao

case class ColumnInfoOneToOne[T, FID, FPC <: DeclaredIds[FID], F](
	val column: OneToOne[FID, FPC, F],
	val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FID, FPC, F] {
	override val getterMethod = None
}
