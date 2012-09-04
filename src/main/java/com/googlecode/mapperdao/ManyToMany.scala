package com.googlecode.mapperdao

case class ManyToMany[FPC, F](linkTable: LinkTable, val foreign: TypeRef[FPC, F]) extends ColumnRelationshipBase[FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = Nil
	override def toString = "manytomany(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
