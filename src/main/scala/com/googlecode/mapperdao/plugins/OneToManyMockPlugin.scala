package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.schema.Type

/**
 * @author kostantinos.kougios
 *
 *         11 Dec 2012
 */
class OneToManyMockPlugin extends SelectMock
{
	override def updateMock[ID, T](
		tpe: Type[ID, T],
		mods: scala.collection.mutable.Map[String, Any]
		) {
		mods ++= tpe.table.oneToManyColumns.map(c => (c.alias -> List()))
	}
}