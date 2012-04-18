package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfo[T, V](
	override val column: SimpleColumn,
	override val columnToValue: T => V,
	val dataType: Class[V])
		extends ColumnInfoBase[T, V](column, columnToValue, None)
