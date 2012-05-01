package com.googlecode.mapperdao

protected abstract class ColumnRelationshipBase[FPC, F](foreign: TypeRef[FPC, F]) extends ColumnBase {
	def columns: List[Column]
	val columnNames = columns.map(_.name).toSet
}
