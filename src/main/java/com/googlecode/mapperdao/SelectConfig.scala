package com.googlecode.mapperdao

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
	lazyLoad: LazyLoad = LazyLoad.defaultLazyLoad)
