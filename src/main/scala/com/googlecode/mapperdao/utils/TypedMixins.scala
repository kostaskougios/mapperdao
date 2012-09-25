package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId
import com.googlecode.mapperdao.StringId

/**
 * provides CRUD methods for entities with IntId
 *
 * @see CRUD
 */
trait IntIdCRUD[T] extends CRUD[Int, IntId, T] {
	/**
	 * links non-persisted entities to the database provided that
	 * the entity has a correct primary key.
	 *
	 * I.e. if you are able to fully recreate the entity (including it's primary keys)
	 * say after posting a form, making sure the entity hmatches the database values,
	 * then you can link it back to mapperdao via the link() method. Then the linked entity
	 * can be used for updates, like if it was loaded from the database. This way a select()
	 * can be avoided.
	 *
	 * val dog=new Dog("Jerry")
	 * val linkedDog=dao.link(dog,5)
	 */
	def link(o: T, id: Int): T with IntId = mapperDao.link(entity, o, id)
}

/**
 * provides CRUD methods for entities with LongId
 *
 * @see CRUD
 */
trait LongIdCRUD[T] extends CRUD[Long, LongId, T] {
	/**
	 * links non-persisted entities to the database provided that
	 * the entity has a correct primary key.
	 *
	 * I.e. if you are able to fully recreate the entity (including it's primary keys)
	 * say after posting a form, making sure the entity matches the database values,
	 * then you can link it back to mapperdao via the link() method. Then the linked entity
	 * can be used for updates, like if it was loaded from the database. This way a select()
	 * can be avoided.
	 *
	 * val dog=new Dog("Jerry")
	 * val linkedDog=dao.link(dog,5)
	 */
	def link(o: T, id: Long): T with LongId = mapperDao.link(entity, o, id)
}

/**
 * these mixin traits add querying methods to a dao. Please see the All trait
 */
trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait StringAll[T] extends All[StringId, T]
