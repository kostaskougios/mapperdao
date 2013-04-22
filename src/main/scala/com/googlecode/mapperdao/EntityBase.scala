package com.googlecode.mapperdao

import com.googlecode.mapperdao.schema.{Schema, PK, Type}

/**
 * all entities inherit this
 *
 * @author: kostas.kougios
 *          Date: 22/04/13
 */
trait EntityBase[ID, T]
{
	def table: String

	def clz: Class[T]

	// cache for table name in lowercase
	private[mapperdao] val tableLower = table.toLowerCase

	private[mapperdao] def tpe: Type[ID, T]

	private[mapperdao] def keysDuringDeclaration: List[PK]

	def databaseSchema: Option[Schema]
}
