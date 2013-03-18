package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao._

/**
 * @author kostantinos.kougios
 *
 *         11 Dec 2012
 */
class OneToOneMockPlugin extends SelectMock {
	override def updateMock[ID, T](
		tpe: Type[ID, T],
		mods: scala.collection.mutable.Map[String, Any]
	) {
		mods ++= tpe.table.oneToOneColumns.map(c => (c.alias -> null))
	}
}