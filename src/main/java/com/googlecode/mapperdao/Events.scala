package com.googlecode.mapperdao

trait DeleteEvent {
	def beforeDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)]) = {}
	def afterDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)]) = {}
}

trait DeleteRelationshipEvent {
	def before[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F])
}

class Events(
	val deleteEvents: List[DeleteEvent] = List(),
	val deleteRelationshipEvents: List[DeleteRelationshipEvent] = List())