package com.googlecode.mapperdao

case class ColumnInfo[T, V](
	val column: SimpleColumn,
	val columnToValue: T => V,
	val dataType: Class[V])
		extends ColumnInfoBase[T, V] {
}
