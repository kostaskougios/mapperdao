package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.EntityBase

abstract class ColumnBase
{
	def alias: String

	def entity: EntityBase[_, _]

	val aliasLowerCase = alias.toLowerCase
}
