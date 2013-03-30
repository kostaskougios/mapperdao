package com.googlecode.mapperdao.customization

import com.googlecode.mapperdao.state.persistcmds.PersistCmd
import org.springframework.jdbc.core.SqlParameterValue
import com.googlecode.mapperdao.Type

/**
 * @author: kostas.kougios
 *          Date: 30/03/13
 */
trait CustomDatabaseToScalaTypes
{
	def transformValuesBeforeStoring(cmd: PersistCmd, sqlValue: SqlParameterValue): SqlParameterValue

	def transformValuesAfterSelecting(tpe: Type[_, _], v: Any): Any
}
