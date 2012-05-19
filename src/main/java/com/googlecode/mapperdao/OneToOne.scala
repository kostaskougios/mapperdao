package com.googlecode.mapperdao

case class OneToOne[FPC, F](foreign: TypeRef[FPC, F], selfColumns: List[Column]) extends ColumnRelationshipBase(foreign) {
	def alias = foreign.alias

	override def columns: List[Column] = selfColumns
	override def toString = "OneToOne(%s,%s)".format(foreign.entity.getClass.getSimpleName, selfColumns.map(_.name).mkString(","))
}
