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
import com.googlecode.mapperdao.PK

/**
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait CachedDriver extends Driver {
	val cache: Cache

	override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]) = {
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
		leftKeyValues: List[(SimpleColumn, Any)]) = {
		val key = manyToMany.linkTable.name :: leftKeyValues

		//		println("\n\n ** cache: " + key + "\n\n")

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
	override def queryForList[PC, T](queryConfig: QueryConfig, tpe: Type[PC, T], sql: String, args: List[Any]) = {
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

	override def doUpdate[PC, T](tpe: Type[PC, T], args: List[(SimpleColumn, Any)], pkArgs: List[(SimpleColumn, Any)]): UpdateResult = {
		val u = super.doUpdate(tpe, args, pkArgs)
		val key = tpe.table.name :: pkArgs
		cache.flush(key)
		u
	}

	override def doDeleteManyToManyRef[PC, T, PR, R](
		tpe: Type[PC, T],
		ftpe: Type[PR, R],
		manyToMany: ManyToMany[_, _],
		leftKeyValues: List[(SimpleColumn, Any)],
		rightKeyValues: List[(SimpleColumn, Any)]): UpdateResult = {
		val u = super.doDeleteManyToManyRef(tpe, ftpe, manyToMany, leftKeyValues, rightKeyValues)
		cache.flush(manyToMany.linkTable.name :: leftKeyValues)
		cache.flush(manyToMany.linkTable.name :: rightKeyValues)
		u
	}

	override def doInsertManyToMany[PC, T, FPC, F](
		tpe: Type[PC, T],
		manyToMany: ManyToMany[FPC, F],
		left: List[Any],
		right: List[Any]): Unit = {

		val u = super.doInsertManyToMany(tpe, manyToMany, left, right)

		val lkey = manyToMany.linkTable.name :: (manyToMany.linkTable.left zip left)

		//		println("\n\n ** flush: " + lkey + "\n\n")

		cache.flush(lkey)
		val rkey = manyToMany.linkTable.name :: (manyToMany.linkTable.right zip right)
		cache.flush(rkey)
		u
	}

	override def doDelete[PC, T](tpe: Type[PC, T], whereColumnValues: List[(SimpleColumn, Any)]) = {
		super.doDelete(tpe, whereColumnValues)
		val key = tpe.table.name :: whereColumnValues
		cache.flush(key)
	}
}