package com.googlecode.mapperdao

/**
 * a one-to-many declared in parent entity
 * and declared as a primary key in child entity
 * i.e.
 * val from = declarePrimaryKey(PersonEntity.linked)
 *
 * @author kostantinos.kougios
 *
 *         17 Aug 2012
 */
class ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, F, TID, T](
	// due to recursing declarations of the ci's, we
	// pass this by-name
	ci: => ColumnInfoTraversableOneToMany[FID, F, TID, T]
	)
{
	def declaredColumnInfo = ci
}

