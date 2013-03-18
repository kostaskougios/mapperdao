package com.googlecode.mapperdao.drivers

/**
 * experimental: provide hints to queries
 *
 * @author kostantinos.kougios
 *
 *         May 11, 2012
 */
case class SelectHints(afterTableName: List[AfterTableNameSelectHint])

object SelectHints
{
	val None = new SelectHints(Nil)
}