package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.{Persisted, Entity}
import com.googlecode.mapperdao.internal.GetterMethod

case class ColumnInfoTraversableOneToMany[ID, T, FID, F](
	column: OneToMany[FID, F],
	columnToValue: (_ >: T) => Traversable[F],
	getterMethod: Option[GetterMethod],
	entityOfT: Entity[ID, Persisted, T]
	)
	extends ColumnInfoRelationshipBase[T, Traversable[F], FID, F]
