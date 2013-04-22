package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.EntityBase

case class ManyToMany[FID, F](
	entity: EntityBase[_, _],
	linkTable: LinkTable,
	foreign: TypeRef[FID, F]
	)
	extends ColumnRelationshipBase[FID, F]
{
	def alias = foreign.alias

	override def columns: List[Column] = Nil

	override def toString = "manytomany(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
