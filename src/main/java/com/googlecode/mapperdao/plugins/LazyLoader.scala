package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.DatabaseValues
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.Entity

/**
 * @author kostantinos.kougios
 *
 * 24 May 2012
 */
abstract class LazyLoader[PC, T] extends (() => Any) {
	@volatile var result: Option[Any] = None
	override def apply() = synchronized {
		if (!result.isDefined) {
			result = Some(calculate)
		}
		result.get
	}

	def calculate: Any
}