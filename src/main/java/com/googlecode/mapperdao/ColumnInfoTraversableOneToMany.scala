package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfoTraversableOneToMany[T, FPC, F](
	override val column: OneToMany[FPC, F],
	override val columnToValue: (_ >: T) => Traversable[F])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F](column, columnToValue, None)
