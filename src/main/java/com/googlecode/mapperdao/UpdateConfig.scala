package com.googlecode.mapperdao

case class UpdateConfig(
	skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
	deleteConfig: DeleteConfig = DeleteConfig(),
	data: Option[Any] = None,
	depth: Int = 1000)

object UpdateConfig {
	val default = UpdateConfig()
}