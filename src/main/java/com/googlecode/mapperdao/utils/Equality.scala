package com.googlecode.mapperdao.utils

import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.ValuesMap
import java.util.Calendar
import org.joda.time.DateTime

/**
 * mapperdao has it's own way of considering equality of objects.
 * Here are utility methods which check for equality.
 *
 * @author kostantinos.kougios
 *
 * 7 Sep 2011
 */
protected[mapperdao] object Equality {

	def isEqual(o1: Any, o2: Any): Boolean = o1 match {
		case a1: Any => o2 match {
			case a2: Any => a1 == a2
			case null => false
		}
		case null => o2 == null
	}

	def onlyChanged(column: ColumnBase, newValuesMap: ValuesMap, oldValuesMap: ValuesMap): Boolean = !isEqual(newValuesMap.valueOf[AnyRef](column.alias), oldValuesMap.valueOf(column.alias))
}