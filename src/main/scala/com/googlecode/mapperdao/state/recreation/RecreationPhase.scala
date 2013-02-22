package com.googlecode.mapperdao.state.recreation

import com.googlecode.mapperdao._
import state.persisted.{ExternalEntityPersistedNode, EntityPersistedNode, PersistedNode}

/**
 * during recreation phase, persisted objects are re-created with Stored type mixed in the
 * new instances.
 *
 * @author kostantinos.kougios
 *
 *         11 Dec 2012
 */
class RecreationPhase(
	                     updateConfig: UpdateConfig,
	                     mockFactory: MockFactory,
	                     typeManager: TypeManager,
	                     entityMap: UpdateEntityMap,
	                     nodes: List[PersistedNode[_, _]]
	                     ) {

	private val byIdentity: Map[Int, PersistedNode[_, _]] = nodes.map {
		node =>
			val id = node.identity
			if (id <= 0)
				throw new IllegalStateException("identity==" + id + " for " + node)
			(id, node)
	}.toMap

	def execute = recreate(updateConfig, nodes.filter(_.mainEntity)).toList

	private def recreate(updateConfig: UpdateConfig, nodes: Traversable[PersistedNode[_, _]]): Traversable[Any] =
		nodes.map {
			node =>
				entityMap.get[Any](node.identity).getOrElse {

					node match {
						case EntityPersistedNode(tpe, oldVM, newVM, _) =>
							val table = tpe.table

							val modified = newVM.toMap

							// create a mock
							val mockO = mockFactory.createMock(updateConfig.data, tpe, modified)
							entityMap.put(node.identity, mockO)

							val related = table.relationshipColumnInfos(updateConfig.skip).map {
								case ColumnInfoTraversableManyToMany(column, _, _) =>
									val mtm = newVM.manyToMany(column)
									val relatedNodes = toNodes(mtm)
									(
										column.alias,
										recreate(updateConfig, relatedNodes)
										)
								case ColumnInfoManyToOne(column, _, _) =>
									val mto = newVM.manyToOne(column)
									if (mto == null) {
										(column.alias, null)
									} else {
										val relatedNodes = toNode(mto) :: Nil
										(column.alias, recreate(updateConfig, relatedNodes).head)
									}
								case ColumnInfoTraversableOneToMany(column, _, _, _) =>
									val otm = newVM.oneToMany(column)
									val relatedNodes = toNodes(otm)
									(
										column.alias,
										recreate(updateConfig, relatedNodes)
										)
								case ColumnInfoOneToOne(column, _) =>
									val oto = newVM.oneToOne(column)
									if (oto == null) {
										(column.alias, null)
									} else {
										val relatedNodes = toNode(oto) :: Nil
										(column.alias, recreate(updateConfig, relatedNodes).head)
									}
								case ColumnInfoOneToOneReverse(column, _, _) =>
									val oto = newVM.oneToOneReverse(column)
									if (oto == null) {
										(column.alias, null)
									} else {
										val relatedNodes = toNode(oto) :: Nil
										(column.alias, recreate(updateConfig, relatedNodes).head)
									}
							}

							val finalMods = modified ++ related
							val finalVM = ValuesMap.fromMap(node.identity, finalMods)
							val newE = tpe.constructor(updateConfig.data, finalVM)
							finalVM.identity = System.identityHashCode(newE)
							// re-put the actual
							entityMap.put(node.identity, newE)
							newE

						case ExternalEntityPersistedNode(entity, o) =>
							o
					}
				}
		}

	private def toNode(a: Any) = a match {
		case _ =>
			byIdentity(System.identityHashCode(a))
	}

	private def toNodes(l: Traversable[Any]) = l.map(toNode(_))
}