package com.googlecode.mapperdao

/**
 * @author: kostas.kougios
 *          Date: 30/05/13
 */
trait QueryBuilder[ID, PC <: Persisted, T]
{
	protected[mapperdao] def entity: EntityBase[ID, T]
}
