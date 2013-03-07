package com.googlecode.mapperdao

case class ColumnInfoOneToOne[T, FID, F](
	column: OneToOne[FID, F],
	columnToValue: (_ >: T) => F
	)
	extends ColumnInfoRelationshipBase[T, F, FID, F]
{
	override val getterMethod = None
}
