package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.EntityBase

case class ManyToOne[FID, F](
	entity: EntityBase[_, _],
	columns: List[Column],
	foreign: TypeRef[FID, F]
	)
	extends ColumnRelationshipBase[FID, F]
{
	def alias = foreign.alias

	override def toString = "manytoone(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
