package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.{Persisted, Entity}

case class OneToMany[FID, F](
	entity: Entity[_, _ <: Persisted, _],
	foreign: TypeRef[FID, F],
	foreignColumns: List[Column]
	) extends ColumnRelationshipBase[FID, F]
{
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns

	override def toString = "onetomany(%s) foreignkey(%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
