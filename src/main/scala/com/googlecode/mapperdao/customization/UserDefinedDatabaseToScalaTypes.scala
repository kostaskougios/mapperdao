package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.state.persistcmds.{UpdateCmd, InsertCmd, PersistCmd}
import com.googlecode.mapperdao.{SimpleColumn, Type}

/**
 * allows custom mapping from scala -> database and from database->scala types
 *
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
abstract class UserDefinedDatabaseToScalaTypes extends CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(cmd: PersistCmd, sqlValues: List[(SimpleColumn, Any)]) = {

		def doConvertion(tpe: Type[_, _]) = {
			sqlValues.map {
				columnValue =>
					scalaToDatabase(tpe, columnValue)
			}
		}

		cmd match {
			case InsertCmd(tpe, _, _, _) =>
				doConvertion(tpe)
			case UpdateCmd(tpe, _, _, _, _) =>
				doConvertion(tpe)
			case _ => sqlValues
		}
	}


	def transformValuesAfterSelecting(tpe: Type[_, _], column: SimpleColumn, v: Any) = databaseToScala(tpe, column, v)

	def scalaToDatabase(tpe: Type[_, _], data: (SimpleColumn, Any)): (SimpleColumn, Any)

	def databaseToScala(tpe: Type[_, _], column: SimpleColumn, v: Any): Any
}
