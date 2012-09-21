package com.googlecode.mapperdao

case class OneToOne[FPC, F](val foreign: TypeRef[FPC, F], selfColumns: List[Column]) extends ColumnRelationshipBase[FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = selfColumns
	override def toString = "onetoone(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, selfColumns.map(_.name).mkString(","))
}
