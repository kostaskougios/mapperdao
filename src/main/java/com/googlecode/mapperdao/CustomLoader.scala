package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.JdbcMap

/**
 * @author kostantinos.kougios
 *
 * 30 May 2012
 */
case class CustomLoader[T, FPC, FT](
	ci: ColumnInfoTraversableManyToMany[T, FPC, FT],
	loader: (SelectConfig, List[JdbcMap]) => List[FT with FPC])