package com.googlecode.mapperdao

protected abstract class ColumnBase
{
	def alias: String

	def entity: Entity[_, _, _]

	val aliasLowerCase = alias.toLowerCase
}
