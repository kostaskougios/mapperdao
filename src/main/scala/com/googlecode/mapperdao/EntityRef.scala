package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.Schema

/**
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
