package com.googlecode.mapperdao.events

import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.ColumnInfoRelationshipBase
import com.googlecode.mapperdao.Type

trait DeleteEvent {
	def beforeDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC)
	def afterDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC)
}

trait DeleteRelationshipEvent {
	def before[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC)
	def after[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC)
}

class Events(
		deleteEvents: List[DeleteEvent] = List(),
		deleteRelationshipEvents: List[DeleteRelationshipEvent] = List()) {
	def executeBeforeDeleteEvents[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.beforeDeleteEntity(tpe, keyValues, entityValue) }
	def executeAfterDeleteEvents[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.afterDeleteEntity(tpe, keyValues, entityValue) }
	def executeBeforeDeleteRelationshipEvents[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.before(tpe, columnInfo, entityValue) }
	def executeAfterDeleteRelationshipEvents[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.after(tpe, columnInfo, entityValue) }
}