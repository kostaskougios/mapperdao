package com.googlecode.mapperdao

case class ColumnInfoOneToOneReverse[T, FID, F](
	column: OneToOneReverse[FID, F],
	columnToValue: (_ >: T) => F,
	getterMethod: Option[GetterMethod]
	)
	extends ColumnInfoRelationshipBase[T, F, FID, F]
