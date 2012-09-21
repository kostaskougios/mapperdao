package com.googlecode.mapperdao

case class ColumnInfoManyToOne[T, FPC, F](
	val column: ManyToOne[FPC, F],
	val columnToValue: (_ >: T) => F,
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, F, FPC, F]
