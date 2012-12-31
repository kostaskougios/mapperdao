package com.googlecode.mapperdao

case class ColumnInfoTraversableManyToMany[T, FID, F](
	val column: ManyToMany[FID, F],
	val columnToValue: T => Traversable[F],
	val getterMethod: Option[GetterMethod]
)
	extends ColumnInfoRelationshipBase[T, Traversable[F], FID, F]
