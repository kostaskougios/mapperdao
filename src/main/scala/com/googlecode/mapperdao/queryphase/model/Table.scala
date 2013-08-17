package com.googlecode.mapperdao.queryphase.model

import com.googlecode.mapperdao.schema.LinkTable


/**
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
case class Table(schema: Option[String], name: String)

object Table
{
	def apply[ID, T](table: com.googlecode.mapperdao.schema.Table[ID, T]): Table =
		Table(table.schemaName, table.name)

	def apply(table: LinkTable): Table = Table(table.schemaName, table.name)
}