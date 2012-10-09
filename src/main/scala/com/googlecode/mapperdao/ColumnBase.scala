package com.googlecode.mapperdao

protected abstract class ColumnBase {
	def alias: String

	val aliasLowerCase = alias.toLowerCase
}
