package com.googlecode.mapperdao

protected abstract class ColumnRelationshipBase[FPC, F] extends ColumnBase {
	def columns: List[Column]
	val columnNames = columns.map(_.name).toSet
	def foreign: TypeRef[FPC, F]
}
