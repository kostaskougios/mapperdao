package com.googlecode.mapperdao

/**
 * a DSL to delete data.
 *
 * @author kostantinos.kougios
 *
 * 17 Oct 2012
 */
object Delete {
	def delete = From

	protected object From {
		def from[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) =
			new Where(entity)
	}

	protected[mapperdao] class Where[ID, PC <: DeclaredIds[ID], T](val entity: Entity[ID, PC, T]) {
		def run(queryDao: QueryDao) = queryDao.delete(this)
	}
}