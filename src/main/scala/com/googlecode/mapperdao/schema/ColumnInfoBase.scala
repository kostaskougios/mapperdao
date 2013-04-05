package com.googlecode.mapperdao.schema

abstract class ColumnInfoBase[T, +V]
{
	val column: ColumnBase
	val columnToValue: T => V
}
