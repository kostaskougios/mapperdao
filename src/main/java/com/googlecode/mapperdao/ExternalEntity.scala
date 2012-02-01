package com.googlecode.mapperdao
import com.googlecode.mapperdao.utils.LazyActions
import com.googlecode.mapperdao.utils.MapWithDefault

/**
 * external entities allow loading entities externally via a custom dao or i.e. hibernate
 *
 * T is the type of the entity.
 *
 * If queries with joins are to be done for this entity and the entity has a table (but is mapped with an other
 * orm or is just loaded via jdbc) then the table and columns can be mapped as normally would be done if the
 * entity was a mapperdao entity.
 */
abstract class ExternalEntity[F](table: String, clz: Class[F]) extends Entity[AnyRef, F](table, clz) {
	def this(clz: Class[F]) = this(clz.getSimpleName, clz)

	private val lazyActions = new LazyActions

	override def constructor(implicit m) = throw new IllegalStateException("constructor shouldn't be called for ExternalEntity %s".format(clz))

	/**
	 * support for many-to-many mapping
	 */
	type OnInsertManyToMany = InsertExternalManyToMany[_, F] => PrimaryKeysValues
	type OnSelectManyToMany = SelectExternalManyToMany => List[F]
	type OnUpdateManyToMany = UpdateExternalManyToMany[_, F] => PrimaryKeysValues
	type OnDeleteManyToMany = DeleteExternalManyToMany[_, F] => Unit
	private[mapperdao] var manyToManyOnInsertMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, F], OnInsertManyToMany]("onInsertManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnSelectMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, F], OnSelectManyToMany]("onSelectManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnUpdateMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, F], OnUpdateManyToMany]("onUpdateManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnDeleteMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, F], OnDeleteManyToMany]("onDeleteManyToMany must be called for External Entity %s".format(getClass.getName))
	def onInsertManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, F])(handler: OnInsertManyToMany) = lazyActions(() => manyToManyOnInsertMap + (ci, handler))
	def onInsertManyToMany(handler: OnInsertManyToMany) { manyToManyOnInsertMap.default = Some(handler) }
	def onSelectManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, F])(handler: OnSelectManyToMany) = lazyActions(() => manyToManyOnSelectMap + (ci, handler))
	def onSelectManyToMany(handler: OnSelectManyToMany) { manyToManyOnSelectMap.default = Some(handler) }
	def onUpdateManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, F])(handler: OnUpdateManyToMany) = lazyActions(() => manyToManyOnUpdateMap + (ci, handler))
	def onUpdateManyToMany(handler: OnUpdateManyToMany) { manyToManyOnUpdateMap.default = Some(handler) }
	def onDeleteManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, F])(handler: OnDeleteManyToMany) = lazyActions(() => manyToManyOnDeleteMap + (ci, handler))
	def onDeleteManyToMany(handler: OnDeleteManyToMany) { manyToManyOnDeleteMap.default = Some(handler) }

	/**
	 * support for one-to-one reverse mapping
	 */

	type OnInsertOneToOneReverse[T] = InsertExternalOneToOneReverse[T, F] => Unit
	type OnSelectOneToOneReverse[T] = SelectExternalOneToOneReverse[T, F] => F
	type OnUpdateOneToOneReverse[T] = UpdateExternalOneToOneReverse[T, F] => Unit
	type OnDeleteOneToOneReverse[T] = DeleteExternalOneToOneReverse[T, F] => Unit
	private[mapperdao] var oneToOneOnInsertMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, F], OnInsertOneToOneReverse[_]]("onInsertOneToOneReverse must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToOneOnSelectMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, F], OnSelectOneToOneReverse[_]]("onSelectOneToOneReverse must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToOneOnUpdateMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, F], OnUpdateOneToOneReverse[_]]("onUpdateOneToOneReverse must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToOneOnDeleteMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, F], OnDeleteOneToOneReverse[_]]("onDeleteOneToOneReverse must be called for External Entity %s".format(getClass.getName))
	def onInsertOneToOneReverse[T](ci: => ColumnInfoOneToOneReverse[T, _, F])(handler: OnInsertOneToOneReverse[T]) = lazyActions(() => oneToOneOnInsertMap + (ci, handler))
	def onInsertOneToOneReverse(handler: OnInsertOneToOneReverse[_]) { oneToOneOnInsertMap.default = Some(handler) }
	def onSelectOneToOneReverse[T](ci: => ColumnInfoOneToOneReverse[T, _, F])(handler: OnSelectOneToOneReverse[T]) = lazyActions(() => oneToOneOnSelectMap + (ci, handler))
	def onSelectOneToOneReverse(handler: OnSelectOneToOneReverse[_]) { oneToOneOnSelectMap.default = Some(handler) }
	def onUpdateOneToOneReverse[T](ci: => ColumnInfoOneToOneReverse[T, _, F])(handler: OnUpdateOneToOneReverse[T]) = lazyActions(() => oneToOneOnUpdateMap + (ci, handler))
	def onUpdateOneToOneReverse(handler: OnUpdateOneToOneReverse[_]) { oneToOneOnUpdateMap.default = Some(handler) }
	def onDeleteOneToOneReverse[T](ci: => ColumnInfoOneToOneReverse[T, _, F])(handler: OnDeleteOneToOneReverse[T]) = lazyActions(() => oneToOneOnDeleteMap + (ci, handler))
	def onDeleteOneToOneReverse(handler: OnDeleteOneToOneReverse[_]) { oneToOneOnDeleteMap.default = Some(handler) }

	/**
	 * support for many-to-one mapping
	 */
	type OnInsertManyToOne = InsertExternalManyToOne[_, F] => PrimaryKeysValues // return the primary keys
	type OnSelectManyToOne = SelectExternalManyToOne[_, F] => F // return the actual one-value
	type OnUpdateManyToOne = UpdateExternalManyToOne[_, F] => PrimaryKeysValues // return the primary keys

	private[mapperdao] val manyToOneOnInsertMap = new MapWithDefault[ColumnInfoManyToOne[_, _, F], OnInsertManyToOne]("onInsertManyToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] val manyToOneOnSelectMap = new MapWithDefault[ColumnInfoManyToOne[_, _, F], OnSelectManyToOne]("onSelectManyToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] val manyToOneOnUpdateMap = new MapWithDefault[ColumnInfoManyToOne[_, _, F], OnUpdateManyToOne]("onUpdateManyToOne must be called for External Entity %s".format(getClass.getName))
	def onInsertManyToOne(ci: => ColumnInfoManyToOne[_, _, F])(handler: OnInsertManyToOne) = lazyActions(() => manyToOneOnInsertMap + (ci, handler))
	def onInsertManyToOne(handler: OnInsertManyToOne) { manyToOneOnInsertMap.default = Some(handler) }
	def onSelectManyToOne(ci: => ColumnInfoManyToOne[_, _, F])(handler: OnSelectManyToOne) = lazyActions(() => manyToOneOnSelectMap + (ci, handler))
	def onSelectManyToOne(handler: OnSelectManyToOne) { manyToOneOnSelectMap.default = Some(handler) }
	def onUpdateManyToOne(ci: => ColumnInfoManyToOne[_, _, F])(handler: OnUpdateManyToOne) = lazyActions(() => manyToOneOnUpdateMap + (ci, handler))
	def onUpdateManyToOne(handler: OnUpdateManyToOne) { manyToOneOnUpdateMap.default = Some(handler) }

	/**
	 * support for one-to-many mapping
	 */
	type OnInsertOneToMany = InsertExternalOneToMany[_, F] => Unit
	type OnSelectOneToMany = SelectExternalOneToMany => List[F]
	type OnUpdateOneToMany = UpdateExternalOneToMany[_, F] => Unit
	private[mapperdao] var oneToManyOnInsertMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, F], OnInsertOneToMany]("onInsertOneToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToManyOnSelectMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, F], OnSelectOneToMany]("onSelectOneToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToManyOnUpdateMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, F], OnUpdateOneToMany]("onUpdateOneToMany must be called for External Entity %s".format(getClass.getName))

	def onInsertOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, F])(handler: OnInsertOneToMany) = lazyActions(() => oneToManyOnInsertMap + (ci, handler))
	def onInsertOneToMany(handler: OnInsertOneToMany) = oneToManyOnInsertMap.default = Some(handler)
	def onSelectOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, F])(handler: OnSelectOneToMany) = lazyActions(() => oneToManyOnSelectMap + (ci, handler))
	def onSelectOneToMany(handler: OnSelectOneToMany) = oneToManyOnSelectMap.default = Some(handler)
	def onUpdateOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, F])(handler: OnUpdateOneToMany) = lazyActions(() => oneToManyOnUpdateMap + (ci, handler))
	def onUpdateOneToMany(handler: OnUpdateOneToMany) = oneToManyOnUpdateMap.default = Some(handler)

	override def init: Unit = {
		super.init
		lazyActions.executeAll
	}
}

case class PrimaryKeysValues(values: List[Any])
object PrimaryKeysValues {
	def apply(value1: Any): PrimaryKeysValues = PrimaryKeysValues(List(value1))
	def apply(value1: Any, value2: Any): PrimaryKeysValues = PrimaryKeysValues(List(value1, value2))
}
case class InsertExternalManyToMany[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalManyToMany(selectConfig: SelectConfig, foreignIds: List[List[Any]] /* a list of the id's as an other list */ )
object UpdateExternalManyToMany {
	object Operation extends Enumeration {
		val Remove, Add = Value
	}
	type Operation = Operation.Value
}
case class UpdateExternalManyToMany[T, F](updateConfig: UpdateConfig, operation: UpdateExternalManyToMany.Operation, entity: T, foreign: F)
case class DeleteExternalManyToMany[T, F](deleteConfig: DeleteConfig, entity: T, foreign: F)

case class InsertExternalOneToOneReverse[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalOneToOneReverse[T, F](selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToOneReverse[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class DeleteExternalOneToOneReverse[T, F](deleteConfig: DeleteConfig, entity: T, foreign: F)

case class InsertExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalManyToOne[T, F](selectConfig: SelectConfig, primaryKeys: List[Any])
case class UpdateExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)

case class InsertExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, many: Traversable[F])
case class SelectExternalOneToMany(selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, added: Traversable[F], intersection: Traversable[F], removed: Traversable[F])
