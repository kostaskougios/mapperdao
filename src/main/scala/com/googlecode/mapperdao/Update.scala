package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 29 Oct 2012
 */
object Update extends SqlImplicitConvertions
		with SqlManyToOneImplicitConvertions
		with SqlOneToOneImplicitConvertions {

	def update[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) =
		new UpdateStart(entity)

	protected class UpdateStart[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) {
		def set = new UpdateSet

		class UpdateSet extends SqlClauses[UpdateSet] {
			def where = new Where
			class Where extends SqlWhereMixins[Where]
		}
	}
}