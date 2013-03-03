package com.googlecode.mapperdao

case class ColumnInfoTraversableOneToMany[ID, T, FID, F](
	val column: OneToMany[FID, F],
	val columnToValue: (_ >: T) => Traversable[F],
	val getterMethod: Option[GetterMethod],
	entityOfT: Entity[ID,Persisted, T]
)
	extends ColumnInfoRelationshipBase[T, Traversable[F], FID, F]
