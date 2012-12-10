package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.state.persisted.PersistedNode
import com.googlecode.mapperdao._

/**
 * plugins executed before the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeInsert {
	def before[PID, PPC <: DeclaredIds[PID], PT, ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		updateInfo: UpdateInfo[PID, PPC, PT, V, FID, FPC, F]): List[(Column, Any)]
}

/**
 * plugins executed after the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostInsert {
	def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
private[mapperdao] class DuringUpdateResults(val values: List[(SimpleColumn, Any)], val keys: List[(SimpleColumn, Any)]) {
	def isEmpty = values.isEmpty && keys.isEmpty

	override def toString = "DuringUpdateResults(values: %s, keys: %s)".format(values, keys)
}

private[mapperdao] object DuringUpdateResults {
	val empty = new DuringUpdateResults(Nil, Nil)
}

trait DuringUpdate {
	def during[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults
}

/**
 * plugins executed after the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostUpdate {
	def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is created, during select operations
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeSelect {
	def idContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		om: DatabaseValues,
		entities: EntityMap): List[Any]
	def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		selectConfig: SelectConfig,
		om: DatabaseValues,
		entities: EntityMap): List[SelectMod]
}

trait SelectMock {
	def updateMock[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		mods: scala.collection.mutable.Map[String, Any])
}

/**
 * plugins executed before deleting an entity
 */
trait BeforeDelete {
	def idColumnValueContribution[ID, PC <: DeclaredIds[ID], T](
		tpe: Type[ID, PC, T],
		deleteConfig: DeleteConfig,
		events: Events,
		o: T with PC,
		entityMap: UpdateEntityMap): List[(SimpleColumn, Any)]
	def before[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		deleteConfig: DeleteConfig,
		events: Events, o: T with PC,
		keyValues: List[(ColumnBase, Any)],
		entityMap: UpdateEntityMap): Unit
}