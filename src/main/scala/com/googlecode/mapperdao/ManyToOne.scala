package com.googlecode.mapperdao

case class ManyToOne[FID, FPC <: DeclaredIds[FID], F](
	columns: List[Column],
	val foreign: TypeRef[FID, FPC, F]
)
	extends ColumnRelationshipBase[FID, FPC, F] {
	def alias = foreign.alias

	override def toString = "manytoone(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
