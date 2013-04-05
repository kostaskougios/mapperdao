package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
trait CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(sqlValues: List[(SimpleColumn, Any)]): List[(SimpleColumn, Any)]

	def transformValuesAfterSelecting(column: SimpleColumn, v: Any): Any
}
