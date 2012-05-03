package com.googlecode.mapperdao

import java.lang.reflect.Method

abstract class ColumnInfoRelationshipBase[T, V, FPC, F](
	val column: ColumnRelationshipBase[FPC, F],
	val columnToValue: T => V,
	val getterMethod: Option[Method])
		extends ColumnInfoBase[T, V]
