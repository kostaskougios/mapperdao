package com.googlecode.mapperdao

case class ColumnInfoManyToOne[T, FPC, F](
	override val column: ManyToOne[FPC, F],
	override val columnToValue: (_ >: T) => F,
	override val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, getterMethod)
