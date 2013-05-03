package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.Schema

/**
 *
 * For cyclic dependent entities it is impossible to construct the Entity objects due to the cyclic dependencies. In that case,
 * an EntityRef can be used as a placeholder for an entity.
 *
 * @author: kostas.kougios
 *          Date: 22/04/13
 */
class EntityRef[ID, T](
	val table: String,
	val clz: Class[T],
	refOf: => EntityBase[ID, T],
	val databaseSchema: Option[Schema] = None
	) extends EntityBase[ID, T]
{
	private[mapperdao] def tpe = refOf.tpe

	private[mapperdao] def keysDuringDeclaration = Nil
}

object EntityRef
{
	def apply[ID, T](table: String, clz: Class[T], refOf: => EntityBase[ID, T], databaseSchema: Option[Schema] = None) = new EntityRef(table, clz, refOf, databaseSchema)
}