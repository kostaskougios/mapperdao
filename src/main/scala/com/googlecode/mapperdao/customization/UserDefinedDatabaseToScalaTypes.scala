package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.SimpleColumn

/**
 * allows custom mapping from scala -> database and from database->scala types
 *
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
abstract class UserDefinedDatabaseToScalaTypes extends CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(sqlValues: List[(SimpleColumn, Any)]) =
		sqlValues.map {
			columnValue =>
				scalaToDatabase(columnValue)
		}


	def transformValuesAfterSelecting(column: SimpleColumn, v: Any) = databaseToScala((column, v))

	def scalaToDatabase(data: (SimpleColumn, Any)): (SimpleColumn, Any)

	def databaseToScala(data: (SimpleColumn, Any)): Any
}
