package com.googlecode.mapperdao

case class ColumnInfoOneToOneReverse[T, FPC, F](
	val column: OneToOneReverse[FPC, F],
	val columnToValue: (_ >: T) => F,
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, F, FPC, F]
