package com.googlecode.mapperdao

case class ColumnInfoTraversableManyToMany[T, FPC, F](
	override val column: ManyToMany[FPC, F],
	override val columnToValue: T => Traversable[F],
	override val getterMethod: Option[GetterMethod])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, getterMethod)
