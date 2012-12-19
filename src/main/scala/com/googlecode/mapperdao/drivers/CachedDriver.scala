package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.jdbc.UpdateResult
import com.googlecode.mapperdao._

/**
 * mixin trait for a Driver that adds caching to Driver's operations.
 *
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait CachedDriver extends Driver {
	val cache: Cache

	override def doSelect[ID, PC <: DeclaredIds[ID], T](selectConfig: SelectConfig, tpe: Type[ID, PC, T], where: List[(SimpleColumn, Any)]) = {
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
	override def doSelectManyToMany[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
		selectConfig: SelectConfig,
		tpe: Type[ID, PC, T],
		ftpe: Type[FID, FPC, F],
		manyToMany: ManyToMany[FID, FPC, F],
		leftKeyValues: List[(SimpleColumn, Any)]) = {
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
	override def queryForList[ID, PC <: DeclaredIds[ID], T](queryConfig: QueryConfig, tpe: Type[ID, PC, T], sql: String, args: List[Any]) = {
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

	override def updateSql[ID, PC <: DeclaredIds[ID], T](
		entity: Entity[ID, PC, T],
		args: List[(SimpleColumn, Any)],
		pkArgs: List[(SimpleColumn, Any)]) = {
		val u = super.updateSql(entity, args, pkArgs)

		val table = entity.tpe.table
		// flush main cache for entity
		val key = table.name :: pkArgs
		cache.flush(key)

		// flush one-to-many caches
		table.oneToManyColumns.foreach { c =>
			val k = c.foreign.entity.tpe.table.name :: (c.columns zip pkArgs.map(_._2))
			cache.flush(k)
		}
		u
	}

	override def doDeleteManyToManyRef[ID, PC <: DeclaredIds[ID], T, PID, PR <: DeclaredIds[PID], R](
		tpe: Type[ID, PC, T],
		ftpe: Type[PID, PR, R],
		manyToMany: ManyToMany[_, _, _],
		leftKeyValues: List[(SimpleColumn, Any)],
		rightKeyValues: List[(SimpleColumn, Any)]): UpdateResult = {
		val u = super.doDeleteManyToManyRef(tpe, ftpe, manyToMany, leftKeyValues, rightKeyValues)
		cache.flush(manyToMany.linkTable.name :: leftKeyValues)
		cache.flush(manyToMany.linkTable.name :: rightKeyValues)
		u
	}

	override def insertManyToManySql(
		manyToMany: ManyToMany[_, _, _],
		left: List[Any],
		right: List[Any]) = {
		val u = super.insertManyToManySql(manyToMany, left, right)

		val lkey = manyToMany.linkTable.name :: (manyToMany.linkTable.left zip left)

		cache.flush(lkey)
		val rkey = manyToMany.linkTable.name :: (manyToMany.linkTable.right zip right)
		cache.flush(rkey)
		u
	}

	override def doDelete[ID, PC <: DeclaredIds[ID], T](tpe: Type[ID, PC, T], whereColumnValues: List[(SimpleColumn, Any)]) = {
		super.doDelete(tpe, whereColumnValues)
		val key = tpe.table.name :: whereColumnValues
		cache.flush(key)
	}
}