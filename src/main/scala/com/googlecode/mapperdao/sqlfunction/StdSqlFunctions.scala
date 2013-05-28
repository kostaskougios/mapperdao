package com.googlecode.mapperdao.sqlfunction

/**
 * standard sql functions widely supported by many/all databases. Please ask for more at:
 *
 * https://groups.google.com/forum/?fromgroups#!forum/mapperdao
 *
 * @author: kostas.kougios
 *          Date: 27/05/13
 */
object StdSqlFunctions
{
	val lower = SqlFunction.with1Arg[String, String]("lower")
	val upper = SqlFunction.with1Arg[String, String]("upper")
}
