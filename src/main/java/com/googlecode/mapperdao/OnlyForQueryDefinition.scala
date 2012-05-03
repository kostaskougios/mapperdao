package com.googlecode.mapperdao

/**
 * declares if a mapping is only used in queries and mapperdao
 * should skip fetching or updating data
 *
 * @author kostantinos.kougios
 *
 * May 3, 2012
 */
trait OnlyForQueryDefinition {
	protected var onlyForQuery = false
	def forQueryOnly(): this.type = {
		onlyForQuery = true
		this
	}
}