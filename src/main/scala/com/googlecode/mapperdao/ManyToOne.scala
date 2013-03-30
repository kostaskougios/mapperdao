package com.googlecode.mapperdao

case class ManyToOne[FID, F](
	entity: Entity[_, _ <: Persisted, _],
	columns: List[Column],
	foreign: TypeRef[FID, F]
	)
	extends ColumnRelationshipBase[FID, F]
{
	def alias = foreign.alias

	override def toString = "manytoone(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
