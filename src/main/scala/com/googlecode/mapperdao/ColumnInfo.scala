package com.googlecode.mapperdao

case class ColumnInfo[T, V](
	column: SimpleColumn,
	columnToValue: T => V,
	dataType: Class[V])
	extends ColumnInfoBase[T, V]
{
}
