package com.googlecode.mapperdao

/**
 * a DSL to delete data.
 *
 * @author ko]]stantinos.kougios
 *
 *         17 Oct 2012
 */
object Delete extends SqlImplicitConvertions
with SqlManyToOneImplicitConvertions
with SqlOneToOneImplicitConvertions {
	def delete = From

	private[mapperdao] trait DeleteDDL[ID, T] {

		private[mapperdao] val entity: Entity[ID, T]

		def run(implicit queryDao: QueryDao) = queryDao.delete(this)
	}

	protected object From {
		def from[ID, T](entity: Entity[ID, T]) =
			new FromOptions(entity)
	}

	protected[mapperdao] class FromOptions[ID, T](private[mapperdao] val entity: Entity[ID, T])
		extends DeleteDDL[ID, T] {
		def where = new Where(this)
	}

	protected[mapperdao] class Where[ID, T](private[mapperdao] val fromOptions: FromOptions[ID, T])
		extends DeleteDDL[ID, T]
		with SqlWhereMixins[Where[ID, T]] {
		private[mapperdao] val entity = fromOptions.entity
	}

}
