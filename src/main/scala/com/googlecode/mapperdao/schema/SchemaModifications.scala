package com.googlecode.mapperdao.schema

/**
 * @author kkougios
 *         Date: 2013/05/28 15:05
 */
class SchemaModifications(
	val tableNameTransformer: String => String
	)

object SchemaModifications
{
	def apply(tableNameTransformer: String => String) = new SchemaModifications(tableNameTransformer)

	val NoOp = new SchemaModifications(
		tableName => tableName
	)
}