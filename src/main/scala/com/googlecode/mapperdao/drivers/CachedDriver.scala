package com.googlecode.mapperdao.drivers

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.{SimpleColumn, ManyToMany}

/**
 * mixin trait for a Driver that adds caching to Driver's operations.
 *
 * @author kostantinos.kougios
 *
 *         21 Mar 2012
 */
trait CachedDriver extends Driver
{
	val cache: Cache

	override def doSelect[ID, T](selectConfig: SelectConfig, tpe: Type[ID, T], where: List[(SimpleColumn, Any)]) = {
		val key = tpe.table.name :: where
		selectConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				val r = super.doSelect(selectConfig, tpe, where)
				cache.put(key, r)
				r
			case co =>
				cache(key, co) {
					super.doSelect(selectConfig, tpe, where)
				}
		}
	}

	override def doSelectManyToMany[ID, T, FID, F](
		selectConfig: SelectConfig,
		tpe: Type[ID, T],
		ftpe: Type[FID, F],
		manyToMany: ManyToMany[FID, F],
		leftKeyValues: List[(SimpleColumn, Any)]
		) = {
		val key = manyToMany.linkTable.name :: leftKeyValues

		selectConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				val r = super.doSelectManyToMany(selectConfig, tpe, ftpe, manyToMany, leftKeyValues)
				cache.put(key, r)
				r
			case co =>
				cache(key, co) {
					super.doSelectManyToMany(selectConfig, tpe, ftpe, manyToMany, leftKeyValues)
				}

		}
	}

	override def queryForList[ID, T](queryConfig: QueryConfig, tpe: Type[ID, T], sql: String, args: List[Any]) = {
		val key = List("query", sql, args)
		queryConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				val r = super.queryForList(queryConfig, tpe, sql, args)
				cache.put(key, r)
				r
			case co =>
				cache(key, co) {
					super.queryForList(queryConfig, tpe, sql, args)
				}
		}
	}

	override def queryForLong(queryConfig: QueryConfig, sql: String, args: List[Any]): Long = {
		val key = List("query", sql, args)
		queryConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				val r = super.queryForLong(queryConfig, sql, args)
				cache.put(key, r)
				r
			case co =>
				cache(key, co) {
					super.queryForLong(queryConfig, sql, args)
				}
		}
	}

	override def updateSql[ID, T](
		tpe: Type[ID, T],
		args: List[(SimpleColumn, Any)],
		pkArgs: List[(SimpleColumn, Any)]
		) = {
		val u = super.updateSql(tpe, args, pkArgs)

		val table = tpe.table
		// flush main cache for entity
		val key = table.name :: pkArgs
		cache.flush(key)

		// flush one-to-many caches
		table.oneToManyColumns.foreach {
			c =>
				val k = c.foreign.entity.tpe.table.name :: (c.columns zip pkArgs.map(_._2))
				cache.flush(k)
		}
		u
	}

	override def deleteManyToManySql(
		manyToMany: ManyToMany[_, _],
		leftKeyValues: List[(SimpleColumn, Any)],
		rightKeyValues: List[(SimpleColumn, Any)]
		) = {
		val u = super.deleteManyToManySql(manyToMany, leftKeyValues, rightKeyValues)
		cache.flush(manyToMany.linkTable.name :: leftKeyValues)
		cache.flush(manyToMany.linkTable.name :: rightKeyValues)
		u
	}

	override def insertManyToManySql(
		manyToMany: ManyToMany[_, _],
		left: List[Any],
		right: List[Any]
		) = {
		val u = super.insertManyToManySql(manyToMany, left, right)

		val lkey = manyToMany.linkTable.name :: (manyToMany.linkTable.left zip left)

		cache.flush(lkey)
		val rkey = manyToMany.linkTable.name :: (manyToMany.linkTable.right zip right)
		cache.flush(rkey)
		u
	}

	override def doDelete[ID, T](tpe: Type[ID, T], whereColumnValues: List[(SimpleColumn, Any)]) {
		super.doDelete(tpe, whereColumnValues)
		val key = tpe.table.name :: whereColumnValues
		cache.flush(key)
	}
}
