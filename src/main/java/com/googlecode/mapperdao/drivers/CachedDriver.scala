package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.SimpleColumn
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.CacheOptions
import com.googlecode.mapperdao.ManyToMany

/**
 * @author kostantinos.kougios
 *
 * 21 Mar 2012
 */
trait CachedDriver extends Driver {
	val cache: Cache

	abstract override def doSelect[PC, T](selectConfig: SelectConfig, tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] =
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
}