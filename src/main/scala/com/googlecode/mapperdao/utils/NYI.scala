package com.googlecode.mapperdao.utils

import java.lang.RuntimeException

/**
 * not yet implemented features
 *
 * @author kostantinos.kougios
 *
 * 5 May 2012
 */
object NYI {
	def apply() = throw new RuntimeException("Not yet implemented, please open a bug at https://code.google.com/p/mapperdao/issues/list . Please copy the full stacktrace of this exception.")
}