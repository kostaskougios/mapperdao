package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.{Persisted, Entity}

case class OneToOne[FID, F](
	entity: Entity[_, _ <: Persisted, _],
	foreign: TypeRef[FID, F],
	selfColumns: List[Column]
	) extends ColumnRelationshipBase[FID, F]
{
	def alias = foreign.alias

	override def columns: List[Column] = selfColumns

	override def toString = "onetoone(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, selfColumns.map(_.name).mkString(","))
}
