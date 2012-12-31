package com.googlecode.mapperdao

case class OneToOne[FID, F](val foreign: TypeRef[FID, F], selfColumns: List[Column])
	extends ColumnRelationshipBase[FID, F] {
	def alias = foreign.alias

	override def columns: List[Column] = selfColumns

	override def toString = "onetoone(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, selfColumns.map(_.name).mkString(","))
}
