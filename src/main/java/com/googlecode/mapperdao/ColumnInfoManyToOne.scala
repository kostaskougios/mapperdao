package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfoManyToOne[T, FPC, F](
	override val column: ManyToOne[FPC, F],
	override val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, None)
