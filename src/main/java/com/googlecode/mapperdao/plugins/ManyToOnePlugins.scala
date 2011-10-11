package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.MapperDao
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.Type
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.Column
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.utils.LowerCaseMutableMap
import com.googlecode.mapperdao.SelectConfig
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.ManyToOne
import com.googlecode.mapperdao.EntityMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneInsertPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDao) extends BeforeInsert {

	override def before[PC, T, V, F](tpe: Type[PC, T], o: T, mockO: T with PC, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], updateInfo: UpdateInfo[Any, V, T]): List[(Column, Any)] =
		{
			val table = tpe.table
			var extraArgs = List[(Column, Any)]()
			// many-to-one
			table.manyToOneColumnInfos.foreach { cis =>
				val fo = cis.columnToValue(o)
				val v = if (fo != null) {
					val fe = typeRegistry.entityOfObject[Any, Any](fo)
					val v = fo match {
						case null => null
						case p: Persisted =>
							entityMap.down(mockO, cis)
							val updated = mapperDao.updateInner(fe, p, entityMap)
							entityMap.up
							updated
						case x =>
							entityMap.down(mockO, cis)
							val inserted = mapperDao.insertInner(fe, x, entityMap)
							entityMap.up
							inserted
					}
					val columns = cis.column.columns.filterNot(table.primaryKeyColumns.contains(_))
					if (!columns.isEmpty && columns.size != cis.column.columns.size) throw new IllegalStateException("only some of the primary keys were declared for %s, and those primary keys overlap manyToOne relationship declaration".format(tpe))
					extraArgs :::= columns zip typeRegistry.typeOf(fe).table.toListOfPrimaryKeyValues(v)
					v
				} else null
				modified(cis.column.alias) = v
			}
			extraArgs
		}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneSelectPlugin(typeRegistry: TypeRegistry, mapperDao: MapperDao) extends BeforeSelect with SelectMock {

	override def idContribution[PC, T](tpe: Type[PC, T], om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]): List[Any] = Nil

	override def before[PC, T](tpe: Type[PC, T], selectConfig: SelectConfig, om: JdbcMap, entities: EntityMap, mods: scala.collection.mutable.HashMap[String, Any]) =
		{
			val table = tpe.table
			// many to one
			table.manyToOneColumnInfos.filterNot(selectConfig.skip(_)).foreach { ci =>
				val c = ci.column.asInstanceOf[ManyToOne[Any]]
				val fe = typeRegistry.entityOf[Any, Any](c.foreign.clz)
				val foreignPKValues = c.columns.map(mtoc => om(mtoc.columnName))
				val fo = entities.get(fe.clz, foreignPKValues)
				val v = if (fo.isDefined) {
					fo.get
				} else {
					entities.down(tpe, ci, om)
					val v = mapperDao.selectInner(fe, selectConfig, foreignPKValues, entities).getOrElse(null)
					entities.up
					v
				}
				mods(c.foreign.alias) = v
			}
		}

	override def updateMock[PC, T](tpe: Type[PC, T], mods: scala.collection.mutable.HashMap[String, Any]) {
		mods ++= tpe.table.manyToOneColumns.map(c => (c.alias -> null))
	}
}

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDao) extends DuringUpdate {

	override def during[PC, T](tpe: Type[PC, T], o: T, oldValuesMap: ValuesMap, newValuesMap: ValuesMap, entityMap: UpdateEntityMap, modified: LowerCaseMutableMap[Any], modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val table = tpe.table

			table.manyToOneColumnInfos.foreach { ci =>
				val v = ci.columnToValue(o)
				val newV = v match {
					case null => null //throw new NullPointerException("unexpected null for primary entity on ManyToOne mapping, for entity %s.".format(o))
					case p: Persisted =>
						val fEntity = typeRegistry.entityOfObject[Any, Any](v)
						entityMap.down(o, ci)
						val newV = mapperDao.updateInner(fEntity, v, entityMap)
						entityMap.up
						newV
					case _ =>
						val fEntity = typeRegistry.entityOfObject[Any, Any](v)
						entityMap.down(o, ci)
						val newV = mapperDao.insertInner(fEntity, v, entityMap)
						entityMap.up
						newV
				}
				modified(ci.column.alias) = newV
			}

			val manyToOneChanged = table.manyToOneColumns.filter(Equality.onlyChanged(_, newValuesMap, oldValuesMap))
			val mtoArgsV = manyToOneChanged.map(mto => (mto.foreign.clz, newValuesMap.valueOf[Any](mto.alias))).map { t =>
				typeRegistry.typeOf(t._1).table.toListOfPrimaryKeyValues(t._2)
			}.flatten
			val cv = (manyToOneChanged.map(_.columns).flatten zip mtoArgsV) filterNot (cav => table.primaryKeyColumns.contains(cav._1))
			new DuringUpdateResults(cv, Nil)
		}
}