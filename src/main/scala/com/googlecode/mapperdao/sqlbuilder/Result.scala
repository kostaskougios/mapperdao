package com.googlecode.mapperdao.sqlbuilder

import org.springframework.jdbc.core.SqlParameterValue

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
case class Result(sql: String, values: List[SqlParameterValue])
