package com.googlecode.mapperdao

case class ManyToOne[FPC, F](columns: List[Column], val foreign: TypeRef[FPC, F]) extends ColumnRelationshipBase[FPC, F] {
	def alias = foreign.alias
	override def toString = "manytoone(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
