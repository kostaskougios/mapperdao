package com.googlecode

/**
 * global utility methods
 *
 * @author: kostas.kougios
 *          Date: 17/04/13
 */
package object mapperdao
{
	def replace[T](oldO: T, newO: T): T = oldO match {
		case p: Persisted =>
			p.mapperDaoReplaced = newO
			newO
		case _ =>
			throw new IllegalArgumentException("can't replace non-persisted entity " + oldO)
	}
}
