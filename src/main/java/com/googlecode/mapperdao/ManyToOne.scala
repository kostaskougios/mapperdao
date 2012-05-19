package com.googlecode.mapperdao

case class ManyToOne[FPC, F](columns: List[Column], foreign: TypeRef[FPC, F]) extends ColumnRelationshipBase(foreign) {
	def name = throw new IllegalStateException("ManyToOne doesn't have a columnName")
	def alias = foreign.alias
	override def toString = "ManyToOne(%s,%s)".format(foreign.entity.getClass.getSimpleName, columns.map(_.name).mkString(","))
}
