package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.UpdateResult

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

		class UpdateSet extends SqlClauses[UpdateSet] with Updatable[ID, PC, T] {
			def where = new Where
			/**
			 * runs the update
			 */
			def run(queryDao: QueryDao) = queryDao.update(this)

			override private[mapperdao] def entity = UpdateStart.this.entity
			override private[mapperdao] def setClauses = clauses

			class Where extends SqlWhereMixins[Where] with Updatable[ID, PC, T] {

				/**
				 * runs the update
				 */
				def run(queryDao: QueryDao) = queryDao.update(this)

				override private[mapperdao] def entity = UpdateStart.this.entity
				override private[mapperdao] def setClauses = UpdateSet.this.setClauses
			}
		}
	}

	trait Updatable[ID, PC <: DeclaredIds[ID], T] {
		private[mapperdao] def entity: Entity[ID, PC, T]
		private[mapperdao] def setClauses: OpBase

		def run(queroDao: QueryDao): UpdateResult
	}
}