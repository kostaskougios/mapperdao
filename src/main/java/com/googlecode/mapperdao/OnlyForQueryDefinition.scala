package com.googlecode.mapperdao

/**
 * declares if a mapping is only used in queries and mapperdao
 * should skip fetching or updating this column
 *
 * @author kostantinos.kougios
 *
 * May 3, 2012
 */
trait OnlyForQueryDefinition {
	protected var onlyForQuery = false

	/**
	 * declare this column as "only for querying". It won't be fetched
	 * from the database but it can be used in queries
	 *
	 * @return		the builder
	 */
	def forQueryOnly(): this.type = {
		onlyForQuery = true
		this
	}
}