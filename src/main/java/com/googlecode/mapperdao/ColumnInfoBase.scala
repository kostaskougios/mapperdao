package com.googlecode.mapperdao

abstract class ColumnInfoBase[T, +V] {
	val column: ColumnBase
	val columnToValue: T => V
}
