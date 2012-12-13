package com.googlecode.mapperdao

case class ManyToMany[FID, FPC <: DeclaredIds[FID], F](
	linkTable: LinkTable,
	val foreign: TypeRef[FID, FPC, F])
		extends ColumnRelationshipBase[FID, FPC, F] {
	def alias = foreign.alias

	override def columns: List[Column] = Nil
	override def toString = "manytomany(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
