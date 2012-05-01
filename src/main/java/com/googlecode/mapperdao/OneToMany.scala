package com.googlecode.mapperdao

case class OneToMany[FPC, F](foreign: TypeRef[FPC, F], foreignColumns: List[Column]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("OneToMany doesn't have a columnName")
	def alias = foreign.alias

	override def columns: List[Column] = foreignColumns
	override def toString = "OneToMany(%s,%s)".format(foreign.entity.getClass.getSimpleName, foreignColumns.map(_.name).mkString(","))
}
