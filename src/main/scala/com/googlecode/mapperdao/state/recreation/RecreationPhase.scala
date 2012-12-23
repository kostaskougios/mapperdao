package com.googlecode.mapperdao.state.recreation

import com.googlecode.mapperdao.state.persisted.PersistedNode
import com.googlecode.mapperdao._

/**
 * during recreation phase, persisted objects are re-created with PC included.
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
			(node.newVM.identity, node)
	}.toMap

	def execute = recreate(nodes.filter(_.mainEntity)).toList

	private def recreate(nodes: Traversable[PersistedNode[_, _]]): Traversable[DeclaredIds[Any]] =
		nodes.map {
			node =>
				entityMap.get[DeclaredIds[Any], Any](node.identity).getOrElse {
					val entity = node.entity
					val tpe = entity.tpe
					val table = tpe.table

					val newVM = node.newVM
					val modified = newVM.toMap

					// create a mock
					val mockO = mockFactory.createMock(updateConfig.data, entity, modified)
					entityMap.put(node.identity, mockO)

					val related = table.relationshipColumnInfos.map {
						case ColumnInfoTraversableManyToMany(column, columnToValue, getterMethod) =>
							val relatedNodes = toNodes(newVM.manyToMany(column))
							(
								column.alias,
								recreate(relatedNodes)
								)
					}.toMap

					val finalMods = modified ++ related
					val newE = tpe.constructor(updateConfig.data, ValuesMap.fromMap(node.identity, finalMods))
					// re-put the actual
					entityMap.put(node.identity, newE)
					newE.asInstanceOf[DeclaredIds[Any]]
				}
		}

	private def toNode(a: Any) = byIdentity.get(System.identityHashCode(a))

	private def toNodes(l: Traversable[Any]) = l.map(toNode(_)).filter(_.isDefined).map(_.get)
}