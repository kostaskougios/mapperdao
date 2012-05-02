package com.googlecode.mapperdao.utils
import com.googlecode.mapperdao.IntId
import com.googlecode.mapperdao.LongId
import com.googlecode.mapperdao.SimpleEntity

trait IntIdCRUD[T] extends CRUD[IntId, T, Int] {
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

trait LongIdCRUD[T] extends CRUD[LongId, T, Long] {
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

trait SimpleCRUD[T, PK] extends CRUD[AnyRef, T, PK] {
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
	 * val linkedDog=dao.link(dog)
	 */
	def link(o: T): T = entity match {
		case se: SimpleEntity[T] => mapperDao.link(se, o)
	}
}

trait IntIdAll[T] extends All[IntId, T]
trait LongIdAll[T] extends All[LongId, T]
trait SimpleAll[T] extends All[AnyRef, T]
