package com.googlecode.mapperdao.plugins

/**
 * @author kostantinos.kougios
 *
 * 24 May 2012
 */
abstract class LazyLoader extends (() => Any) {
	@volatile var result: Option[Any] = None
	override def apply() = synchronized {
		if (!result.isDefined) {
			result = Some(calculate)
		}
		result.get
	}

	def calculate: Any
}