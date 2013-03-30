package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.SimpleColumn

/**
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
object DefaultDatabaseToScalaTypes extends CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(sqlValues: List[(SimpleColumn, Any)]) = sqlValues

	def transformValuesAfterSelecting(column: SimpleColumn, v: Any) = v
}
