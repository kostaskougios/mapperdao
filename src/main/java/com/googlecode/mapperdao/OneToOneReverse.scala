package com.googlecode.mapperdao

case class OneToOneReverse[FPC, F](val foreign: TypeRef[FPC, F], foreignColumns: List[Column]) extends ColumnRelationshipBase[FPC, F] {
	def name = throw new IllegalStateException("OneToOneReverse doesn't have a columnName")
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns
	override def toString = "OneToOneReverse(%s,%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
