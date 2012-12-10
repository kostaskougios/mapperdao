package com.googlecode.mapperdao.plugins

import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.utils.Equality
import com.googlecode.mapperdao.utils.MapOfList
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.utils.Helpers
import com.googlecode.mapperdao.DeclaredIds
import com.googlecode.mapperdao.state.persisted.PersistedNode

/**
 * @author kostantinos.kougios
 *
 * 31 Aug 2011
 */
class ManyToOneUpdatePlugin(typeRegistry: TypeRegistry, mapperDao: MapperDaoImpl) extends DuringUpdate {

	override def during[ID, PC <: DeclaredIds[ID], T](
		updateConfig: UpdateConfig,
		node: PersistedNode[ID, T],
		entityMap: UpdateEntityMap,
		modified: scala.collection.mutable.Map[String, Any],
		modifiedTraversables: MapOfList[String, Any]): DuringUpdateResults =
		{
			val entity = node.entity
			val newVM = node.newVM
			val oldVM = node.oldVM
			val tpe = entity.tpe
			val table = tpe.table
			val mtoColumnInfos = node.manyToOne.filterNot(t => updateConfig.skip.contains(t._1))
			mtoColumnInfos.foreach {
				case (cis, childNode) =>
					val v = newVM(cis)

					cis.column.foreign.entity match {
						case ee: ExternalEntity[Any, Any] =>
							modified(cis.column.alias) = v
						case fe: Entity[Any, DeclaredIds[Any], Any] =>
							val newV = v.asInstanceOf[AnyRef] match {
								case null => null
								case p: DeclaredIds[Any] =>
									entityMap.down(null, cis, entity)
									val newV = mapperDao.updateInner(updateConfig, childNode, entityMap)
									entityMap.up
									newV
								case _ =>
									entityMap.down(null, cis, entity)
									val newV = mapperDao.insertInner(updateConfig, childNode, entityMap)
									entityMap.up
									newV
							}
							modified(cis.column.alias) = newV
					}
			}

			val mtoColumns = mtoColumnInfos.map(_._1.column)
			val manyToOneChanged = mtoColumns.filter(Equality.onlyChanged(_, newVM, oldVM))
			val mtoArgsV = manyToOneChanged.map(mto => (mto, mto.foreign.entity, newVM.valueOf[Any](mto))).map {
				case (column, entity, entityO) =>
					entity match {
						case ee: ExternalEntity[Any, Any] =>
							val cis = table.columnToColumnInfoMap(column)
							val v = newVM.valueOf[Any](cis) // cis.columnToValue(o)
							val handler = ee.manyToOneOnUpdateMap(cis.asInstanceOf[ColumnInfoManyToOne[_, _, _, Any]])
								.asInstanceOf[ee.OnUpdateManyToOne[T]]
							handler(UpdateExternalManyToOne(updateConfig, newVM, v)).values
						case e: Entity[Any, DeclaredIds[Any], Any] =>
							e.tpe.table.toListOfPrimaryKeyValues(entityO)
					}
			}.flatten
			val cv = (manyToOneChanged.map(_.columns).flatten zip mtoArgsV) filterNot { case (column, _) => table.primaryKeysAsColumns.contains(column) }
			new DuringUpdateResults(cv, Nil)
		}
}