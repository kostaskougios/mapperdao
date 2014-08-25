package com.googlecode.mapperdao.sqlbuilder

import com.googlecode.mapperdao.schema.SimpleColumn

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
class ColumnAndColumnClause(
	sqlBuilder: SqlBuilder,
	leftAlias: Symbol, leftColumn: SimpleColumn,
	op: String,
	rightAlias: Symbol, rightColumn: SimpleColumn
	) extends NonValueClause(sqlBuilder, leftAlias, leftColumn.name, op, rightAlias, rightColumn.name)
