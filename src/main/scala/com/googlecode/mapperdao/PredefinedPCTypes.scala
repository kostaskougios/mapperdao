package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 *         24 Sep 2012
 */

trait NoId extends DeclaredIds[Unit]

trait NaturalStringId extends DeclaredIds[String]

trait NaturalIntId extends DeclaredIds[Int]

trait NaturalLongId extends DeclaredIds[Long]

trait NaturalIntAndNaturalIntIds extends DeclaredIds[(Int, Int)]

trait NaturalIntAndNaturalLongIds extends DeclaredIds[(Int, Long)]

trait NaturalIntAndNaturalStringIds extends DeclaredIds[(Int, String)]

trait NaturalStringAndStringIds extends DeclaredIds[(String, String)]

trait SurrogateIntAndNaturalStringId extends DeclaredIds[(Int, String)] {
	def id: Int
}

trait SurrogateIntAndNaturalLongId extends DeclaredIds[(Int, Long)] {
	def id: Int
}

trait SurrogateLongAndNaturalStringId extends DeclaredIds[(Long, String)] {
	def id: Long
}

trait SurrogateLongAndNaturalLongId extends DeclaredIds[(Long, Long)] {
	def id: Long
}

trait With1Id[ID1] extends DeclaredIds[ID1]

trait With2Ids[ID1, ID2] extends DeclaredIds[(ID1, ID2)]

trait With3Ids[ID1, ID2, ID3] extends DeclaredIds[(ID1, ID2, ID3)]

/**
 * Classes (mutable or immutable) mapped to tables with surrogate integer id can mix in this trait so that the id can be accessed when required.
 * Note that the id is not part of a domain model but rather part of the database. So a clean domain model class doesn't
 * have to provide access to it's id. But when the entity is loaded from the database, then the id becomes available as the entity
 * becomes a T with IntId.
 *
 * @author kostantinos.kougios
 *
 *         3 Aug 2011
 */
trait SurrogateIntId extends DeclaredIds[Int] {
	def id: Int
}

/**
 * Classes (mutable or immutable) mapped to tables with surrogate long id can mix in this trait so that the id can be accessed when required.
 * Note that the id is not part of a domain model but rather part of the database. So a clean domain model class doesn't
 * have to provide access to it's id. But when the entity is loaded from the database, then the id is available as the
 * entity becomes a T with SurrogateLongId.
 *
 * For nested entities, use Helpers.longIdFor(o) to access the id of an entity.
 *
 * @author kostantinos.kougios
 *
 *         5 Aug 2011
 */
trait SurrogateLongId extends DeclaredIds[Long] {
	def id: Long
}
