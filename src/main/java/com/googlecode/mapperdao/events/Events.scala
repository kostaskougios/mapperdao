package com.googlecode.mapperdao.events

import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.ColumnInfoRelationshipBase
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.ColumnBase

trait DeleteEvent {
	def beforeDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC)
	def afterDeleteEntity[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC)
}

trait DeleteRelationshipEvent {
	def before[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC)
	def after[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC)
}

trait InsertEvent {
	def before[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): Unit
	def after[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): Unit
}

trait UpdateEvent {
	def before[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit
	def after[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit
}

trait SelectEvent {
	def before[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): Unit
	def after[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): Unit
}

class Events(
	deleteEvents: List[DeleteEvent] = List(),
	deleteRelationshipEvents: List[DeleteRelationshipEvent] = List(),
	insertEvents: List[InsertEvent] = List(),
	updateEvents: List[UpdateEvent] = List(),
	selectEvents: List[SelectEvent] = List()) {
	def executeBeforeDeleteEvents[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.beforeDeleteEntity(tpe, keyValues, entityValue) }
	def executeAfterDeleteEvents[PC, T](tpe: Type[PC, T], keyValues: List[(SimpleColumn, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.afterDeleteEntity(tpe, keyValues, entityValue) }
	def executeBeforeDeleteRelationshipEvents[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.before(tpe, columnInfo, entityValue) }
	def executeAfterDeleteRelationshipEvents[PC, T, V, F](tpe: Type[PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.after(tpe, columnInfo, entityValue) }
	def executeBeforeInsertEvents[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): Unit = insertEvents.foreach { _.before(tpe, args) }
	def executeAfterInsertEvents[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): Unit = insertEvents.foreach { _.after(tpe, args) }
	def executeBeforeUpdateEvents[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit = updateEvents.foreach { _.before(tpe, args, pkArgs) }
	def executeAfterUpdateEvents[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit = updateEvents.foreach { _.after(tpe, args, pkArgs) }
	def executeBeforeSelectEvents[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): Unit = selectEvents.foreach { _.before(tpe, where) }
	def executeAfterSelectEvents[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): Unit = selectEvents.foreach { _.after(tpe, where) }
}