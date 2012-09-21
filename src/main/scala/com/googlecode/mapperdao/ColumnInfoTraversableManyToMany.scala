package com.googlecode.mapperdao

case class ColumnInfoTraversableManyToMany[T, FPC, F](
	val column: ManyToMany[FPC, F],
	val columnToValue: T => Traversable[F],
	val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F]
