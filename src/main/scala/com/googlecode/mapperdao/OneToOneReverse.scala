package com.googlecode.mapperdao

case class OneToOneReverse[FID, F](foreign: TypeRef[FID, F], foreignColumns: List[Column])
	extends ColumnRelationshipBase[FID, F]
{
	def name = throw new IllegalStateException("OneToOneReverse doesn't have a columnName")

	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns

	override def toString = "onetoonereverse(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
