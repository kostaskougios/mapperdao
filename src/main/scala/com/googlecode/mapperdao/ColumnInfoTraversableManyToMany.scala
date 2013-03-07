package com.googlecode.mapperdao

case class ColumnInfoTraversableManyToMany[T, FID, F](
	column: ManyToMany[FID, F],
	columnToValue: T => Traversable[F],
	getterMethod: Option[GetterMethod]
	)
	extends ColumnInfoRelationshipBase[T, Traversable[F], FID, F]
