package com.googlecode.mapperdao

case class OneToMany[FPC, F](val foreign: TypeRef[FPC, F], foreignColumns: List[Column]) extends ColumnRelationshipBase[FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns
	override def toString = "onetomany(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
