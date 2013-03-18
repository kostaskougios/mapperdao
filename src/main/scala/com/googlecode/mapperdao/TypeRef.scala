package com.googlecode.mapperdao

case class TypeRef[FID, F](
	alias: String,
	entity: Entity[FID, Persisted, F]
	)
