package com.googlecode.mapperdao.schema


case class ColumnInfo[T, V](
	column: SimpleColumn,
	columnToValue: T => V,
	dataType: Class[V])
	extends ColumnInfoBase[T, V]
{
}
