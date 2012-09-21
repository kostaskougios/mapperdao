package com.googlecode.mapperdao

import java.io.InputStream
import org.springframework.jdbc.core.support.SqlLobValue

/**
 * @author kostantinos.kougios
 *
 * 14 Sep 2012
 */
case class Blob(in: InputStream, length: Int) {
	private[mapperdao] def toSqlLobValue = new SqlLobValue(in, length)
}