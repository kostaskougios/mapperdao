package com.googlecode.mapperdao

/**
 * @param propagate		Will the delete be propagated to related entities?
 * @param skip			if propagate=true, skip relationships will be skipped. If propagate=false, this is not used
 *
 * example: DeleteConfig(true,Set(Product.attributes)) // propagate deletes but not for attributes
 */
case class DeleteConfig(propagate: Boolean = false, skip: Set[ColumnInfoRelationshipBase[_, _, _, _]] = Set())
