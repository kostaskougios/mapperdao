package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.{ParQueryRunStrategy, QueryRunStrategy, DefaultQueryRunStrategy}

/**
 * configuration for running queries using multiple
 * threads.
 *
 * Warning: multi threaded queries ignore transactions. Entities
 * retrieved using a multi-threaded query might contain
 * out-of-transaction data.
 *
 * @author kostantinos.kougios
 *
 *         6 May 2012
 */
abstract class MultiThreadedConfig
{
	// having a lot of small groups will speed up entities with few relations. Less but bigger
	// groups speed up entities with many relations
	val inGroupsOf: Int
	val runStrategy: QueryRunStrategy
}

object MultiThreadedConfig
{

	object Single extends MultiThreadedConfig
	{
		val inGroupsOf = -1
		val runStrategy = new DefaultQueryRunStrategy
	}

	object Multi extends MultiThreadedConfig
	{
		val inGroupsOf = 128
		val runStrategy = new ParQueryRunStrategy
	}

	abstract class CustomMulti extends MultiThreadedConfig
	{
		val runStrategy = new ParQueryRunStrategy
	}

}