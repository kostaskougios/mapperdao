package com.googlecode.mapperdao
import com.googlecode.mapperdao.drivers.SelectHints

/**
 * mapperDao.select configuration.
 *
 * @param	skip	skip one or more relationships from loading. If skipped, a traversable will
 * 					be empty and a reference to an other entity will be null
 * @param	data	any kind of data
 *
 * example: SelectConfig(skip=Set(ProductEntity.attributes)) // attributes won't be loaded
 */
case class SelectConfig(
	skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(),
	data: Option[Any] = None,
	cacheOptions: CacheOption = CacheOptions.NoCache,
	lazyLoad: LazyLoad = LazyLoad.none,
	hints: SelectHints = SelectHints.None)

object SelectConfig {
	def from(queryConfig: QueryConfig) =
		SelectConfig(
			skip = queryConfig.skip,
			data = queryConfig.data,
			cacheOptions = queryConfig.cacheOptions,
			lazyLoad = queryConfig.lazyLoad,
			hints = queryConfig.hints
		)

	def lazyLoad = SelectConfig(lazyLoad = LazyLoad.all)

	val default = SelectConfig()
}