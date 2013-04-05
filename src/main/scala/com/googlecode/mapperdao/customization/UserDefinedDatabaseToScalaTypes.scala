package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * allows custom mapping from scala -> database and from database->scala types
 *
 * example: CustomTypesSuite
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

	/**
	 * converts a scala type to a database type.
	 *
	 * @param data      a tuple with the column and value that needs to be converted.
	 *
	 * @return          a tuple with both the column and value converted. Can be same
	 *                  as data if no convertion must occur. Note: if the type of the
	 *                  value chances, then simpleColumn.tpe must reflect the new
	 *                  class of the value.
	 */
	def scalaToDatabase(data: (SimpleColumn, Any)): (SimpleColumn, Any)

	/**
	 * converts a database type to scala type. This is the reverse function of scalaToDatabase
	 *
	 * @param data      as they are loaded from the database
	 * @return          the converted value (or the same value if no convertion is applicable)
	 */
	def databaseToScala(data: (SimpleColumn, Any)): Any
}
