package com.googlecode.mapperdao

abstract class ColumnInfoRelationshipBase[T, V, FPC, F](
	val column: ColumnRelationshipBase[FPC, F],
	val columnToValue: T => V,
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoBase[T, V]
