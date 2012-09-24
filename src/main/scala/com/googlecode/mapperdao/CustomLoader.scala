package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * a data loader config for many to many relationships.
 *
 * @author kostantinos.kougios
 *
 * 30 May 2012
 */
case class CustomLoader[T, FPC, FT](
	ci: ColumnInfoTraversableManyToMany[T, FPC, FT],
	loader: (SelectConfig, List[JdbcMap]) => List[FT with FPC])