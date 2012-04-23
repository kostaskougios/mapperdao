package com.googlecode.mapperdao

case class UpdateConfig(
	skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
	deleteConfig: DeleteConfig = DeleteConfig())
