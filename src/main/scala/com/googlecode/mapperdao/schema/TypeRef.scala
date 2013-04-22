package com.googlecode.mapperdao.schema

import com.googlecode.mapperdao.EntityBase

case class TypeRef[FID, F](
	alias: String,
	entity: EntityBase[FID, F]
	)
