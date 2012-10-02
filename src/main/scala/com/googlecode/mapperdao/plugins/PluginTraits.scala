package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.events.Events
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.DeclaredIds

/**
 * plugins executed before the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeInsert {
	def before[PPC <: DeclaredIds[_], PT, PC <: DeclaredIds[_], T, V, FPC <: DeclaredIds[_], F](
		updateConfig: UpdateConfig,
		entity: Entity[PC, T],
		o: T,
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		updateInfo: UpdateInfo[PPC, PT, V, FPC, F]): List[(Column, Any)]
}

/**
 * plugins executed after the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostInsert {
	def after[PC <: DeclaredIds[_], T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): Unit
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
	def during[PC <: DeclaredIds[_], T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults
}

/**
 * plugins executed after the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostUpdate {
	def after[PC <: DeclaredIds[_], T](updateConfig: UpdateConfig, entity: Entity[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is created, during select operations
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeSelect {
	def idContribution[PC <: DeclaredIds[_], T](tpe: Type[PC, T], om: DatabaseValues, entities: EntityMap): List[Any]
	def before[PC <: DeclaredIds[_], T](entity: Entity[PC, T], selectConfig: SelectConfig, om: DatabaseValues, entities: EntityMap): List[SelectMod]
}

trait SelectMock {
	def updateMock[PC <: DeclaredIds[_], T](entity: Entity[PC, T], mods: scala.collection.mutable.Map[String, Any])
}

/**
 * plugins executed before deleting an entity
 */
trait BeforeDelete {
	def idColumnValueContribution[PC <: DeclaredIds[_], T](tpe: Type[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, entityMap: UpdateEntityMap): List[(SimpleColumn, Any)]
	def before[PC <: DeclaredIds[_], T](entity: Entity[PC, T], deleteConfig: DeleteConfig, events: Events, o: T with PC with Persisted, keyValues: List[(ColumnBase, Any)], entityMap: UpdateEntityMap): Unit
}