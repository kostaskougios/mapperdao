package com.googlecode.mapperdao.events

import com.googlecode.mapperdao._

trait DeleteEvent {
	def beforeDeleteEntity[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], keyValues: List[(ColumnBase, Any)], entityValue: T with PC)
	def afterDeleteEntity[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], keyValues: List[(ColumnBase, Any)], entityValue: T with PC)
}

trait DeleteRelationshipEvent {
	def before[ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](tpe: Type[ID, PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, FID, FPC, F], entityValue: T with PC)
	def after[ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](tpe: Type[ID, PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, FID, FPC, F], entityValue: T with PC)
}

trait InsertEvent {
	def before[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)]): Unit
	def after[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)]): Unit
}

trait UpdateEvent {
	def before[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit
	def after[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit
}

trait SelectEvent {
	def before[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], where: List[(SimpleColumn, Any)]): Unit
	def after[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], where: List[(SimpleColumn, Any)]): Unit
}

class Events(
		deleteEvents: List[DeleteEvent] = List(),
		deleteRelationshipEvents: List[DeleteRelationshipEvent] = List(),
		insertEvents: List[InsertEvent] = List(),
		updateEvents: List[UpdateEvent] = List(),
		selectEvents: List[SelectEvent] = List()) {
	def executeBeforeDeleteEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], keyValues: List[(ColumnBase, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.beforeDeleteEntity(tpe, keyValues, entityValue) }
	def executeAfterDeleteEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], keyValues: List[(ColumnBase, Any)], entityValue: T with PC): Unit = deleteEvents.foreach { _.afterDeleteEntity(tpe, keyValues, entityValue) }
	def executeBeforeDeleteRelationshipEvents[ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](tpe: Type[ID, PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, FID, FPC, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.before(tpe, columnInfo, entityValue) }
	def executeAfterDeleteRelationshipEvents[ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](tpe: Type[ID, PC, T], columnInfo: ColumnInfoRelationshipBase[T, V, FID, FPC, F], entityValue: T with PC) = deleteRelationshipEvents.foreach { _.after(tpe, columnInfo, entityValue) }
	def executeBeforeInsertEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)]): Unit = insertEvents.foreach { _.before(tpe, args) }
	def executeAfterInsertEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)]): Unit = insertEvents.foreach { _.after(tpe, args) }
	def executeBeforeUpdateEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit = updateEvents.foreach { _.before(tpe, args, pkArgs) }
	def executeAfterUpdateEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): Unit = updateEvents.foreach { _.after(tpe, args, pkArgs) }
	def executeBeforeSelectEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], where: List[(SimpleColumn, Any)]): Unit = selectEvents.foreach { _.before(tpe, where) }
	def executeAfterSelectEvents[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], where: List[(SimpleColumn, Any)]): Unit = selectEvents.foreach { _.after(tpe, where) }
}