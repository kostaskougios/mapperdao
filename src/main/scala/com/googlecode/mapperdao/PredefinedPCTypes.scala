package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 24 Sep 2012
 */

trait NoId extends DeclaredIds[Nothing]

trait NaturalStringId extends DeclaredIds[String]

trait NaturalIntId extends DeclaredIds[Int]
trait NaturalLongId extends DeclaredIds[Long]

trait NaturalIntAndNaturalIntIds extends DeclaredIds[(Int, Int)]
trait NaturalIntAndNaturalLongIds extends DeclaredIds[(Int, Long)]
trait NaturalIntAndNaturalStringIds extends DeclaredIds[(Int, String)]

trait NaturalStringAndStringIds extends DeclaredIds[(String, String)]

trait SurrogateIntAndNaturalStringId extends DeclaredIds[(Int, String)] {
	val id: Int
}

trait SurrogateIntAndNaturalLongId extends DeclaredIds[(Int, Long)] {
	val id: Int
}

trait With1Id[ID1] extends DeclaredIds[ID1]
trait With2Ids[ID1, ID2] extends DeclaredIds[(ID1, ID2)]
trait With3Ids[ID1, ID2, ID3] extends DeclaredIds[(ID1, ID2, ID3)]