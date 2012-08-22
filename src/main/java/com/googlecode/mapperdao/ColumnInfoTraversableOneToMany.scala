package com.googlecode.mapperdao

case class ColumnInfoTraversableOneToMany[T, FPC, F](
	val column: OneToMany[FPC, F],
	val columnToValue: (_ >: T) => Traversable[F],
	val getterMethod: Option[GetterMethod],
	entityOfT: Entity[_, T])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FPC, F]
