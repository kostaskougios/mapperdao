package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.TypeRef

abstract class ColumnRelationshipBase[FID, F] extends ColumnBase
{
	def columns: List[Column]

	val columnNames = columns.map(_.name).toSet

	def foreign: TypeRef[FID, F]
}
