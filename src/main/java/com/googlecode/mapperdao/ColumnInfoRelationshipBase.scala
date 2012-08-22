package com.googlecode.mapperdao

abstract class ColumnInfoRelationshipBase[T, V, FPC, F] extends ColumnInfoBase[T, V] {
	val column: ColumnRelationshipBase[FPC, F]
	val columnToValue: T => V
	val getterMethod: Option[GetterMethod]
}

