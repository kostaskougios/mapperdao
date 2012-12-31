package com.googlecode.mapperdao

case class ColumnInfoManyToOne[T, FID, F](
	val column: ManyToOne[FID, F],
	val columnToValue: (_ >: T) => F,
	val getterMethod: Option[GetterMethod]
)
	extends ColumnInfoRelationshipBase[T, F, FID, F]
