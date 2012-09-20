package com.googlecode.mapperdao

/**
 * Classes (mutable or immutable) with integer id's can mix this trait so that the id can be accessed when required.
 * Note that the id is not part of a domain model but rather part of the database. So a clean domain model class doesn't
 * have to provide access to it's id. But when the entity is loaded from the database, then it becomes
 * a T with IntId.
 *
 * Typically this will be used when declaring the entity, i.e. object ProductEntity extends Entity[IntId,Product]
 *
 * @author kostantinos.kougios
 *
 * 3 Aug 2011
 */
trait IntId {
	val id: Int
}