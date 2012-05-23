package com.googlecode.mapperdao

case class ColumnInfoOneToOneReverse[T, FPC, F](
	override val column: OneToOneReverse[FPC, F],
	override val columnToValue: (_ >: T) => F,
	override val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, getterMethod)
