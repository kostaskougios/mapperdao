package com.googlecode.mapperdao

case class OneToOneReverse[FID, FPC <: DeclaredIds[FID], F](val foreign: TypeRef[FID, FPC, F], foreignColumns: List[Column])
		extends ColumnRelationshipBase[FID, FPC, F] {
	def name = throw new IllegalStateException("OneToOneReverse doesn't have a columnName")
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns
	override def toString = "onetoonereverse(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
