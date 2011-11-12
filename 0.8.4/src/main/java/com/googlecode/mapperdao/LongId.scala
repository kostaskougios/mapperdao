package com.googlecode.mapperdao

/**
 * Classes (mutable or immutable) with long id's can mix this trait so that the id can be accessed when required.
 * Note that the id is not part of a domain model but rather part of the database. So a clean domain model class doesn't
 * have to provide access to it's id. Id access should only provided when necessary, i.e. passing the id as a parameter
 * on a url.
 *
 * use MapperDao.longIdFor(o) to access the id of an entity
 *
 * @author kostantinos.kougios
 *
 * 5 Aug 2011
 */
trait LongId {
	val id: Long
}