package com.googlecode.mapperdao

abstract class ColumnInfoRelationshipBase[T, V, FID, FPC <: DeclaredIds[FID], F] extends ColumnInfoBase[T, V] {
	val column: ColumnRelationshipBase[FID, FPC, F]
	val columnToValue: T => V
	val getterMethod: Option[GetterMethod]
}

