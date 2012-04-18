package com.googlecode.mapperdao

import java.lang.reflect.Method

abstract class ColumnInfoBase[T, V](
	val column: ColumnBase,
	val columnToValue: T => V,
	val getterMethod: Option[Method])
