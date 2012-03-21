package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
object CacheOptions {

	/**
	 * no caching
	 */
	object NoCache extends CacheOption {
		def expireInMillis = -1
	}

	/**
	 * data from the database will be cached for 1 second
	 */
	object OneSecond extends CacheOption {
		def expireInMillis = 1000
	}

	object OneMinute extends CacheOption {
		def expireInMillis = 60 * 1000
	}

	object OneHour extends CacheOption {
		def expireInMillis = 60 * 60 * 1000
	}

	object OneDay extends CacheOption {
		def expireInMillis = 24 * 60 * 60 * 1000
	}
}

trait CacheOption {
	def expireInMillis: Long
}