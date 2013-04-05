package com.googlecode.mapperdao

import com.googlecode.mapperdao.drivers.SelectHints
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase

/**
 * mapperDao.select configuration.
 *
 * @param	skip	skip one or more relationships from loading. If skipped, a traversable will
 *                   be empty and a reference to an other entity will be null
 * @param	data	any kind of data that will be passed on to Entity.constructor
 *
 * @param	lazyLoad	configure lazy loading of related entities. Please see LazyLoad class
 *
 * @param	hints	pass on extra hints to the driver (database specific)
 *
 * @param	manyToManyCustomLoaders		(optimization) Load many to many relationships manually
 *
 *                                          example: SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
 */
case class SelectConfig(
	skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
	data: Option[Any] = None,
	cacheOptions: CacheOption = CacheOptions.NoCache,
	lazyLoad: LazyLoad = LazyLoad.none,
	hints: SelectHints = SelectHints.None,
	manyToManyCustomLoaders: List[CustomLoader[_, _, _]] = Nil
	)
{

	def loaderFor(ci: ColumnInfoRelationshipBase[_, _, _, _]) = manyToManyCustomLoaders.find(_.ci == ci)
}

object SelectConfig
{
	def from(queryConfig: QueryConfig) =
		SelectConfig(
			skip = queryConfig.skip,
			data = queryConfig.data,
			cacheOptions = queryConfig.cacheOptions,
			lazyLoad = queryConfig.lazyLoad,
			hints = queryConfig.hints
		)

	def lazyLoad = SelectConfig(lazyLoad = LazyLoad.all)

	// use this to avoid creating instances for the default select config
	val default = SelectConfig()
}