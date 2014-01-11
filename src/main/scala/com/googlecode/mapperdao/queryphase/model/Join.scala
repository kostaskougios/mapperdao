package com.googlecode.mapperdao.queryphase.model

/**
 * join to table
 *
 * @author: kostas.kougios
 *          Date: 13/08/13
 */
case class Join(table: InQueryTable, on: OnClause)
