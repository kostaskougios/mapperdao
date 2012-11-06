package com.googlecode.mapperdao

/**
 * a DSL to delete data.
 *
 * @author ko]]stantinos.kougios
 *
 * 17 Oct 2012
 */
object Delete extends SqlImplicitConvertions
		with SqlManyToOneImplicitConvertions
		with SqlOneToOneImplicitConvertions {
	def delete = From

	private[mapperdao] trait DeleteDDL[ID, PC <: DeclaredIds[ID], T] {

		private[mapperdao] val entity: Entity[ID, PC, T]

		def run(implicit queryDao: QueryDao) = queryDao.delete(this)
	}

	protected object From {
		def from[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) =
			new FromOptions(entity)
	}

	protected[mapperdao] class FromOptions[ID, PC <: DeclaredIds[ID], T](private[mapperdao] val entity: Entity[ID, PC, T])
			extends DeleteDDL[ID, PC, T] {
		def where = new Where(this)
	}

	protected[mapperdao] class Where[ID, PC <: DeclaredIds[ID], T](private[mapperdao] val fromOptions: FromOptions[ID, PC, T])
			extends DeleteDDL[ID, PC, T]
			with SqlWhereMixins[Where[ID, PC, T], OpBase] {
		private[mapperdao] val entity = fromOptions.entity
	}
}
