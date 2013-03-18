package com.googlecode.mapperdao

abstract class ColumnInfoRelationshipBase[T, V, FID, F] extends ColumnInfoBase[T, V]
{
	val column: ColumnRelationshipBase[FID, F]
	val columnToValue: T => V
	val getterMethod: Option[GetterMethod]
}

