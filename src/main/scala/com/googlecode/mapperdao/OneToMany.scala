package com.googlecode.mapperdao

case class OneToMany[FID, F](val foreign: TypeRef[FID, F], foreignColumns: List[Column])
	extends ColumnRelationshipBase[FID, F] {
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns

	override def toString = "onetomany(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
