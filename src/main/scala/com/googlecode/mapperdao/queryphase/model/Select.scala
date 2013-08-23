package com.googlecode.mapperdao.queryphase.model

/**
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
case class Select(
	from: From,
	joins: List[Join],
	where: Clause = NoClause
	)