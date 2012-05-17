package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.CacheOptions
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.QueryConfig
import com.googlecode.mapperdao.ColumnBase
import com.googlecode.mapperdao.jdbc.UpdateResult

/**
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait CachedDriver extends Driver {
	val cache: Cache

	override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] = {
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
	override def doSelectManyToMany[PC, T, FPC, F](
		selectConfig: SelectConfig,
		tpe: Type[PC, T],
		ftpe: Type[FPC, F],
		manyToMany: ManyToMany[FPC, F],
		leftKeyValues: List[(SimpleColumn, Any)]): List[JdbcMap] = {
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
	override def queryForList(queryConfig: QueryConfig, sql: String, args: List[Any]): List[JdbcMap] = {
		val key = List("query", sql, args)
		queryConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				val r = super.queryForList(queryConfig, sql, args)
				cache.put(key, r)
				r
			case co =>
				cache(key, co) {
					super.queryForList(queryConfig, sql, args)
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

	override def doUpdate[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): UpdateResult = {
		val u = super.doUpdate(tpe, args, pkArgs)
		val key = tpe.table.name :: pkArgs
		cache.flush(key)
		u
	}
}