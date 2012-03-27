package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.CacheOptions
import com.googlecode.mapperdao.ManyToMany
import com.googlecode.mapperdao.QueryConfig

/**
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait CachedDriver extends Driver {
	val cache: Cache

	override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] =
		selectConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				super.doSelect(selectConfig, tpe, where)
			case co =>
				val key = tpe.table.name :: where
				cache(key, co) {
					super.doSelect(selectConfig, tpe, where)
				}
		}

	override def doSelectManyToMany[PC, T, FPC, F](selectConfig: SelectConfig, tpe: Type[PC, T],
		ftpe: Type[FPC, F], manyToMany: ManyToMany[FPC, F], leftKeyValues: List[(SimpleColumn, Any)]): List[JdbcMap] =
		selectConfig.cacheOptions match {
			case CacheOptions.NoCache =>
				super.doSelect(selectConfig, tpe, leftKeyValues)
			case co =>
				val key = manyToMany.linkTable.name :: leftKeyValues
				cache(key, co) {
					super.doSelectManyToMany(selectConfig, tpe, ftpe, manyToMany, leftKeyValues)
				}

		}

	override def queryForList(queryConfig: QueryConfig, sql: String, args: List[Any]): List[JdbcMap] = queryConfig.cacheOptions match {
		case CacheOptions.NoCache =>
			super.queryForList(queryConfig, sql, args)
		case co =>
			val key = List("query", sql, args)
			cache(key, co) {
				super.queryForList(queryConfig, sql, args)
			}
	}

	override def queryForLong(queryConfig: QueryConfig, sql: String, args: List[Any]): Long = queryConfig.cacheOptions match {
		case CacheOptions.NoCache =>
			super.queryForLong(queryConfig, sql, args)
		case co =>
			val key = List("query", sql, args)
			cache(key, co) {
				super.queryForLong(queryConfig, sql, args)
			}
	}
}