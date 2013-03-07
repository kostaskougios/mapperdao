package com.googlecode.mapperdao

case class ColumnInfoManyToOne[T, FID, F](
	column: ManyToOne[FID, F],
	columnToValue: (_ >: T) => F,
	getterMethod: Option[GetterMethod]
	)
	extends ColumnInfoRelationshipBase[T, F, FID, F]
