package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfoTraversableManyToMany[T, FPC, F](
	override val column: ManyToMany[FPC, F],
	override val columnToValue: T => Traversable[F],
	override val getterMethod: Option[Method])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, getterMethod)
