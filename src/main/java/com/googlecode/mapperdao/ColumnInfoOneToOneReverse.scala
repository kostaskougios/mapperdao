package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfoOneToOneReverse[T, FPC, F](
	override val column: OneToOneReverse[FPC, F],
	override val columnToValue: (_ >: T) => F)
		extends ColumnInfoRelationshipBase[T, F, FPC, F](column, columnToValue, None)
