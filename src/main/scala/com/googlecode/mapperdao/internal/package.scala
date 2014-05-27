package com.googlecode.mapperdao

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author	kostas.kougios
 *            Date: 27/05/14
 */
package object internal
{
	// generates id's for entities
	private val idGenerator = new AtomicInteger

	private[mapperdao] def nextId = idGenerator.incrementAndGet

}
