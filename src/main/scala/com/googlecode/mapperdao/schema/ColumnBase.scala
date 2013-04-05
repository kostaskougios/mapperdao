package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.Entity

abstract class ColumnBase
{
	def alias: String

	def entity: Entity[_, _, _]

	val aliasLowerCase = alias.toLowerCase
}
