package com.googlecode.mapperdao

/**
 * represents a database function and can be used in queries
 *
 * @author kostantinos.kougios
 *
 * 5 Sep 2012
 */
object SqlFunction {
	def with1Arg[V1, R](name: String) = new SqlFunctionValue1[V1, R](name)
	def with2Args[V1, V2, R](name: String) = new SqlFunctionValue2[V1, V2, R](name)
}