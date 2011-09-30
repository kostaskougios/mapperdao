package com.googlecode.mapperdao.plugins
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.DeleteConfig
import com.googlecode.mapperdao.SimpleColumn

/**
 * plugins executed before the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeInsert {
	def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)]
}

/**
 * plugins executed after the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostInsert {
	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
private[mapperdao] class DuringUpdateResults(val values: List[(Column, Any)], val keys: List[(Column, Any)]) {
	def isEmpty = values.isEmpty && keys.isEmpty

	override def toString = "DuringUpdateResults(values: %s, keys: %s)".format(values, keys)
}

trait DuringUpdate {
	def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults
}

/**
 * plugins executed after the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostUpdate {
	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is created, during select operations
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeSelect {
	def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any]
	def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): Unit
}

trait SelectMock {
	def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any])
}

/**
 * plugins executed before deleting an entity
 */
trait BeforeDelete {
	def before[PC, T](tpe: Type[PC, T], deleteConfig: DeleteConfig, o: T with PC with Persisted, keyValues: List[(SimpleColumn, Any)]): Unit
}