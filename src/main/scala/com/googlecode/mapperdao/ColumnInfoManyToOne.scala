package com.googlecode.mapperdao

case class ColumnInfoManyToOne[T, FID, FPC <: DeclaredIds[FID], F](
	val column: ManyToOne[FID, FPC, F],
	val columnToValue: (_ >: T) => F,
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, F, FID, FPC, F]
