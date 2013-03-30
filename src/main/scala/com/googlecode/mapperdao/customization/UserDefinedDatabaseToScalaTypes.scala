package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.state.persistcmds.{InsertCmd, PersistCmd}
import org.springframework.jdbc.core.SqlParameterValue
import com.googlecode.mapperdao.Type

/**
 * allows custom mapping from scala -> database and from database->scala types
 *
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
abstract class UserDefinedDatabaseToScalaTypes extends CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(cmd: PersistCmd, sqlValue: SqlParameterValue) = cmd match {
		case InsertCmd(tpe, _, columns, _) =>
			val (sqlType, newV) = scalaToDatabase(tpe, sqlValue.getSqlType, sqlValue.getValue)
			new SqlParameterValue(sqlType, newV)
		case _ => sqlValue
	}


	def transformValuesAfterSelecting(tpe: Type[_, _], v: Any) = databaseToScala(tpe, v)

	def scalaToDatabase(tpe: Type[_, _], sqlType: Int, oldV: Any): (Int, Any)

	def databaseToScala(tpe: Type[_, _], v: Any): Any
}
