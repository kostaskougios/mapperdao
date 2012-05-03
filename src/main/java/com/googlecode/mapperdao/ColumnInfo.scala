package com.googlecode.mapperdao

import java.lang.reflect.Method

case class ColumnInfo[T, V](
	val column: SimpleColumn,
	val columnToValue: T => V,
	val dataType: Class[V])
		extends ColumnInfoBase[T, V] {
	val getterMethod = None
}
