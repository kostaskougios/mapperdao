package com.googlecode.mapperdao.exceptions

import com.googlecode.mapperdao.ColumnBase

/**
 * @author kostantinos.kougios
 *
 * 25 Oct 2012
 */
class ColumnNotPartOfQueryException(val column: ColumnBase)
	extends IllegalStateException("A column is not part of a query: " + column.alias + ". Alias not found for " + column)