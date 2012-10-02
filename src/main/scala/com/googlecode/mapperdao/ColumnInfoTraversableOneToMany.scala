package com.googlecode.mapperdao

case class ColumnInfoTraversableOneToMany[T, FID, FPC <: DeclaredIds[FID], F](
	val column: OneToMany[FID, FPC, F],
	val columnToValue: (_ >: T) => Traversable[F],
	val getterMethod: Option[GetterMethod],
	entityOfT: Entity[_, _, T])
		extends ColumnInfoRelationshipBase[T, Traversable[F], FID, FPC, F]
