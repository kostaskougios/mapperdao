package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.drivers.Driver
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.Entity
import com.googlecode.mapperdao.ExternalEntity
import com.googlecode.mapperdao.MapperDaoImpl
import com.googlecode.mapperdao.Persisted
import com.googlecode.mapperdao.TypeManager
import com.googlecode.mapperdao.TypeRegistry
import com.googlecode.mapperdao.UpdateConfig
import com.googlecode.mapperdao.UpdateEntityMap
import com.googlecode.mapperdao.UpdateExternalOneToOneReverse
import com.googlecode.mapperdao.UpdateInfo
import com.googlecode.mapperdao.ValuesMap
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.state.persisted.PersistedNode

class OneToOneReverseUpdatePlugin(typeRegistry: TypeRegistry, typeManager: TypeManager, driver: Driver, mapperDao: MapperDaoImpl) extends DuringUpdate with PostUpdate {
	private val emptyDUR = new DuringUpdateResults(Nil, Nil)

	override def during[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val UpdateInfo(parent, parentColumnInfo, parentEntity) = entityMap.peek[Any, DeclaredIds[Any], Any, T, Any, DeclaredIds[Any], Any]
			if (parent != null) {
				parentColumnInfo match {
					case otor: ColumnInfoOneToOneReverse[_, _, _, T] =>
						val parentTpe = parentEntity.tpe
						new DuringUpdateResults(Nil, otor.column.foreignColumns zip parentTpe.table.toListOfPrimaryKeyValues(parent))
					case _ => emptyDUR
				}
			} else emptyDUR
		}

	def after[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		mockO: T with PC,
		entityMap: UpdateEntityMap,
		modified: MapOfList[String, Any]) =
		{
			val entity = node.entity
			val newVM = node.newVM
			val oldVM = node.oldVM
			val tpe = entity.tpe
			val table = tpe.table
			// one-to-one-reverse
			node.oneToOneReverse.filterNot(t => updateConfig.skip.contains(t._1)).foreach {
				case (ci, childNode) =>
					val fo = newVM.valueOf[Any](ci)
					val c = ci.column

					c.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							val handler = ee.oneToOneOnUpdateMap(ci.asInstanceOf[ColumnInfoOneToOneReverse[T, _, _, Any]])
								.asInstanceOf[ee.OnUpdateOneToOneReverse[T]]
							handler(UpdateExternalOneToOneReverse(updateConfig, newVM, fo))
						case fe: Entity[Any, DeclaredIds[Any], Any] =>
							val ftpe = fe.tpe
							if (fo != null) {
								val v = fo match {
									case p: DeclaredIds[Any] =>
										entityMap.down(mockO, ci, entity)
										mapperDao.updateInner(updateConfig, childNode, entityMap)
										entityMap.up
									case newO =>
										entityMap.down(mockO, ci, entity)
										val oldV = oldVM(ci)
										if (oldV == null) {
											mapperDao.insertInner(updateConfig, childNode, entityMap)
										} else {
											val nVM = ValuesMap.fromEntity(typeManager, ftpe, fo)
											mapperDao.updateInner(updateConfig, childNode, entityMap)
										}
										entityMap.up
								}
							} else {
								val oldV: Any = oldVM.valueOf(c)
								if (oldV != null) {
									// delete the old value from the database
									val args = c.foreignColumns zip newVM.toListOfColumnValue(tpe.table.primaryKeys)
									driver.doDelete(ftpe, args)
								}
							}
					}
			}
		}
}