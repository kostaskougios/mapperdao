package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.utils.TraversableSeparation
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class OneToManyInsertPlugin(typeRegistry: TypeRegistry, driver: Driver, mapperDao: MapperDaoImpl) extends BeforeInsert with PostInsert {

	override def before[PID, PPC <: DeclaredIds[PID], PT, ID, PC <: DeclaredIds[ID], T, V, FID, FPC <: DeclaredIds[FID], F](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		updateInfo: UpdateInfo[PID, PPC, PT, V, FID, FPC, F]): List[(Column, Any)] =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = updateInfo

			if (parent != null) {
				val entity = node.entity
				val parentColumn = parentColumnInfo.column
				parentColumn match {
					case otm: OneToMany[_, _, _] =>
						val parentTpe = parentEntity.tpe
						val tpe = entity.tpe
						val table = tpe.table
						val foreignKeyColumns = otm.foreignColumns
							.filterNot(table.primaryKeysAsColumns.contains(_))
							.filterNot(table.simpleTypeColumns.contains(_))
						if (!foreignKeyColumns.isEmpty) {
							val parentTable = parentTpe.table
							val parentKeysAndValues = parent.asInstanceOf[Persisted]
								.mapperDaoValuesMap.toListOfColumnAndValueTuple(parentTable.primaryKeys)
							val foreignKeys = parentKeysAndValues.map(_._2)
							if (foreignKeys.size != foreignKeyColumns.size)
								throw new IllegalArgumentException("mappings of one-to-many is invalid. Number of FK columns doesn't match primary keys. columns: " + foreignKeyColumns + " , primary key values " + foreignKeys);
							val extra = foreignKeyColumns zip foreignKeys
							// values map should be aware of these columns
							extra.foreach {
								case (c, v) =>
									modified(c.name) = v
							}
							extra
						} else Nil
					case _ => Nil
				}
			} else Nil
		}

	override def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): Unit =
		{
			val entity = node.entity
			val newVM = node.newVM
			val tpe = entity.tpe
			val table = tpe.table
			// one to many
			node.oneToMany.foreach {
				case (cis, childNode) =>
					val traversable = newVM(cis) // cis.columnToValue(o)
					cis.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							val cName = cis.column.alias
							traversable.foreach {
								modifiedTraversables(cName) = _
							}
							val handler = ee.oneToManyOnInsertMap(cis.asInstanceOf[ColumnInfoTraversableOneToMany[_, _, T, _, _, Any]])
								.asInstanceOf[ee.OnInsertOneToMany[T]]
							handler(InsertExternalOneToMany(updateConfig, newVM, traversable))

						case fe: Entity[Any, DeclaredIds[Any], Any] =>
							val ftpe = fe.tpe
							val newKeyValues = table.primaryKeys.map(c => modified(c.name))
							if (traversable != null) {
								traversable.foreach { nested =>
									val newO = if (mapperDao.isPersisted(nested)) {
										val OneToMany(foreign: TypeRef[_, _, _], foreignColumns: List[Column]) = cis.column
										// update
										val keyArgs = ftpe.table.toListOfColumnAndValueTuples(ftpe.table.primaryKeys, nested)
										driver.doUpdateOneToManyRef(ftpe, foreignColumns zip newKeyValues, keyArgs)
										nested
									} else {
										// insert
										entityMap.down(mockO, cis, entity)
										val inserted = mapperDao.insertInner(updateConfig, childNode, entityMap)
										entityMap.up
										inserted
									}
									val cName = cis.column.alias
									modifiedTraversables(cName) = newO
								}
							}
					}
			}
		}
}
