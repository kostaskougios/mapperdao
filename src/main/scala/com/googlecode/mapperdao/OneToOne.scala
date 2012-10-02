package com.googlecode.mapperdao

case class OneToOne[FID, FPC <: DeclaredIds[FID], F](val foreign: TypeRef[FID, FPC, F], selfColumns: List[Column])
		extends ColumnRelationshipBase[FID, FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = selfColumns
	override def toString = "onetoone(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, selfColumns.map(_.name).mkString(","))
}
