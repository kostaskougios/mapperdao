package com.googlecode.mapperdao

case class ColumnInfoOneToOneReverse[T, FID, F](
	val column: OneToOneReverse[FID, F],
	val columnToValue: (_ >: T) => F,
	val getterMethod: Option[GetterMethod]
)
	extends ColumnInfoRelationshipBase[T, F, FID, F]
