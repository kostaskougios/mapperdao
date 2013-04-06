package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.{Persisted, Entity}

case class TypeRef[FID, F](
	alias: String,
	entity: Entity[FID, Persisted, F]
	)
