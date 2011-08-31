package com.rits.orm.plugins
import com.rits.orm.utils.MapOfList
import com.rits.orm.ValuesMap
import com.rits.orm.UpdateEntityMap
import com.rits.orm.Type
import com.rits.orm.Column
import com.rits.orm.UpdateInfo
import com.rits.orm.Persisted

/**
 * plugins executed before the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait BeforeInsert {
	def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], updateInfo: UpdateInfo[Persisted, V, T]): List[(Column, Any)]
}

/**
 * plugins executed after the main entity is inserted
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait PostInsert {
	def after[PC, T](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: scala.collection.mutable.Map[String, Any], modifiedTraversables: MapOfList[String, Any]): Unit
}

/**
 * plugins executed before the main entity is updated
 *
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
trait DuringUpdate {
	def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap): List[(Column, Any)]
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