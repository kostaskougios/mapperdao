package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * a data loader config for many to many relationships.
 *
 * @author kostantinos.kougios
 *
 *         30 May 2012
 */
case class CustomLoader[T, FID, FT](
	ci: ColumnInfoTraversableManyToMany[T, FID, FT],
	loader: (SelectConfig, List[JdbcMap]) => List[FT with DeclaredIds[FID]]
	)