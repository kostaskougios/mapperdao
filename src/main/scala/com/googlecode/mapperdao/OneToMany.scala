package com.googlecode.mapperdao

case class OneToMany[FID, FPC <: DeclaredIds[FID], F](val foreign: TypeRef[FID, FPC, F], foreignColumns: List[Column])
		extends ColumnRelationshipBase[FID, FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns
	override def toString = "onetomany(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
