package com.googlecode.mapperdao

import java.lang.reflect.Method

abstract class ColumnInfoRelationshipBase[T, V, FPC, F](
	override val column: ColumnRelationshipBase[FPC, F],
	override val columnToValue: T => V,
	override val getterMethod: Option[Method])
		extends ColumnInfoBase[T, V](column, columnToValue, getterMethod)
