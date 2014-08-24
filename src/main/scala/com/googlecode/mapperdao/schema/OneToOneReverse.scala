package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.EntityBase

import scala.language.{existentials, implicitConversions}

case class OneToOneReverse[FID, F](
	entity: EntityBase[_, _],
	foreign: TypeRef[FID, F],
	foreignColumns: List[Column]
	) extends ColumnRelationshipBase[FID, F]
{
	def name = throw new IllegalStateException("OneToOneReverse doesn't have a columnName")

	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns

	override def toString = "onetoonereverse(%s) foreignkey (%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
