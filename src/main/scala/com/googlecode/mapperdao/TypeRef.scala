package com.googlecode.mapperdao

case class TypeRef[FID, FPC <: DeclaredIds[FID], F](alias: String, entity: Entity[FID, FPC, F])
