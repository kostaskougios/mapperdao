package com.googlecode.mapperdao.drivers

/**
 * @author kostantinos.kougios
 *
 * 9 Jul 2012
 */
trait EscapeNamesStrategy {
	def escapeColumnNames(name: String): String
	def escapeTableNames(name: String): String
}