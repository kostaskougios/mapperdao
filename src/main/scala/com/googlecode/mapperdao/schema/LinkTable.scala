package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.queries.v2.Alias

/**
 * @author: kostas.kougios
 *          Date: 30/05/13
 */
case class LinkTable(schema: Option[Schema], name: String, left: List[Column], right: List[Column])
{
	if (schema == null) throw new NullPointerException("databaseSchema should be declared first thing in an entity, for " + name)
	val schemaName = schema.map(_.name)
	val entityId = Entity.idGenerator.incrementAndGet
	val alias = Alias.aliasFor(this)
}
