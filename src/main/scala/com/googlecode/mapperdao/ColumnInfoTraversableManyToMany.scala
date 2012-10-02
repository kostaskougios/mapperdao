package com.googlecode.mapperdao

case class ColumnInfoTraversableManyToMany[T, FID, FPC <: DeclaredIds[FID], F](
	val column: ManyToMany[FID, FPC, F],
	val columnToValue: T => Traversable[F],
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FID, FPC, F]
