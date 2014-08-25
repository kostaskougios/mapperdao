package com.googlecode.mapperdao.sqlbuilder

/**
 * @author	kostas.kougios
 *            Date: 25/08/14
 */
abstract class Combine extends Expression
{
	val left: Expression
	val right: Expression
}
