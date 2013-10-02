package com.googlecode.mapperdao.queries.v2

import com.googlecode.mapperdao.schema.ColumnBase

/**
 * @author: kostas.kougios
 *          Date: 02/10/13
 */
case class AliasColumn[C <: ColumnBase](column: C, symbol: Option[Symbol] = None)