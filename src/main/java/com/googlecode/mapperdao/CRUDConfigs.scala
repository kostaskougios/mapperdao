package com.googlecode.mapperdao

/**
 * configuration for fine tuning queries & updates
 *
 * @author kostantinos.kougios
 *
 * 28 Sep 2011
 */

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
	cacheOptions: CacheOption = CacheOptions.NoCache)

/**
 * @param propagate		Will the delete be propagated to related entities?
 * @param skip			if propagate=true, skip relationships will be skipped. If propagate=false, this is not used
 *
 * example: DeleteConfig(true,Set(Product.attributes)) // propagate deletes but not for attributes
 */
case class DeleteConfig(propagate: Boolean = false, skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set())

case class UpdateConfig(skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set(), deleteConfig: DeleteConfig = DeleteConfig())
