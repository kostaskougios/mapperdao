package com.googlecode

import com.googlecode.mapperdao.schema.Type

/**
 * global utility methods
 *
 * @author: kostas.kougios
 *          Date: 17/04/13
 */
package object mapperdao
{
	/**
	 * replace an immutable instance with an other one
	 *
	 * @param oldO      the persisted entity
	 * @param newO      the new value for oldO
	 * @return          a new copy of newO that can be used as a replacement for oldO. Note: newO.neq(return value).
	 *                  Only the return value can be used as a replacement for oldO
	 */
	def replace[T](oldO: T, newO: T): T = oldO match {
		case p: Persisted =>
			val tpe = p.mapperDaoPersistedDetails.entity.tpe
			val newVM = ValuesMap.fromType(p.mapperDaoPersistedDetails.typeManager, tpe.asInstanceOf[Type[Any, Any]], newO, p.mapperDaoValuesMap)
			val n = tpe.constructor(p.mapperDaoPersistedDetails, None, newVM)
			n.asInstanceOf[T]
		case _ =>
			throw new IllegalArgumentException("can't replace non-persisted entity " + oldO)
	}
}
