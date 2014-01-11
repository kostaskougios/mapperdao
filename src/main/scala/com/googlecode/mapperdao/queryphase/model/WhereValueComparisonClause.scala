package com.googlecode.mapperdao.queryphase.model

/**
 * @author: kostas.kougios
 *          Date: 28/08/13
 */
case class WhereValueComparisonClause(left: Column, operator: String, right: String) extends Clause