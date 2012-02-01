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
abstract class ExternalEntity[T](table: String, clz: Class[T]) extends Entity[AnyRef, T](table, clz) {
	def this(clz: Class[T]) = this(clz.getSimpleName, clz)

	private val lazyActions = new LazyActions

	override def constructor(implicit m) = throw new IllegalStateException("constructor shouldn't be called for ExternalEntity %s".format(clz))

	/**
	 * support for many-to-many mapping
	 */
	type OnInsertManyToMany = InsertExternalManyToMany[_, T] => PrimaryKeysValues
	type OnSelectManyToMany = SelectExternalManyToMany => List[T]
	type OnUpdateManyToMany = UpdateExternalManyToMany[_, T] => PrimaryKeysValues
	type OnDeleteManyToMany = DeleteExternalManyToMany[_, T] => Unit
	private[mapperdao] var manyToManyOnInsertMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, T], OnInsertManyToMany]("onInsertManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnSelectMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, T], OnSelectManyToMany]("onSelectManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnUpdateMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, T], OnUpdateManyToMany]("onUpdateManyToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var manyToManyOnDeleteMap = new MapWithDefault[ColumnInfoTraversableManyToMany[_, _, T], OnDeleteManyToMany]("onDeleteManyToMany must be called for External Entity %s".format(getClass.getName))
	def onInsertManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnInsertManyToMany) = lazyActions(() => manyToManyOnInsertMap + (ci, handler))
	def onInsertManyToMany(handler: OnInsertManyToMany) { manyToManyOnInsertMap.default = Some(handler) }
	def onSelectManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnSelectManyToMany) = lazyActions(() => manyToManyOnSelectMap + (ci, handler))
	def onSelectManyToMany(handler: OnSelectManyToMany) { manyToManyOnSelectMap.default = Some(handler) }
	def onUpdateManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnUpdateManyToMany) = lazyActions(() => manyToManyOnUpdateMap + (ci, handler))
	def onUpdateManyToMany(handler: OnUpdateManyToMany) { manyToManyOnUpdateMap.default = Some(handler) }
	def onDeleteManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnDeleteManyToMany) = lazyActions(() => manyToManyOnDeleteMap + (ci, handler))
	def onDeleteManyToMany(handler: OnDeleteManyToMany) { manyToManyOnDeleteMap.default = Some(handler) }

	/**
	 * support for one-to-one reverse mapping
	 */

	type OnInsertOneToOne = InsertExternalOneToOne[_, T] => Unit
	type OnSelectOneToOne = SelectExternalOneToOne[_, T] => T
	type OnUpdateOneToOne = UpdateExternalOneToOne[_, T] => Unit
	private[mapperdao] var oneToOneOnInsertMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, T], OnInsertOneToOne]("onInsertOneToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToOneOnSelectMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, T], OnSelectOneToOne]("onSelectOneToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToOneOnUpdateMap = new MapWithDefault[ColumnInfoOneToOneReverse[_, _, T], OnUpdateOneToOne]("onUpdateOneToOne must be called for External Entity %s".format(getClass.getName))
	def onInsertOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnInsertOneToOne) = lazyActions(() => oneToOneOnInsertMap + (ci, handler))
	def onInsertOneToOne(handler: OnInsertOneToOne) { oneToOneOnInsertMap.default = Some(handler) }
	def onSelectOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnSelectOneToOne) = lazyActions(() => oneToOneOnSelectMap + (ci, handler))
	def onSelectOneToOne(handler: OnSelectOneToOne) { oneToOneOnSelectMap.default = Some(handler) }
	def onUpdateOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnUpdateOneToOne) = lazyActions(() => oneToOneOnUpdateMap + (ci, handler))
	def onUpdateOneToOne(handler: OnUpdateOneToOne) { oneToOneOnUpdateMap.default = Some(handler) }

	/**
	 * support for many-to-one mapping
	 */
	type OnInsertManyToOne = InsertExternalManyToOne[_, T] => PrimaryKeysValues // return the primary keys
	type OnSelectManyToOne = SelectExternalManyToOne[_, T] => T // return the actual one-value
	type OnUpdateManyToOne = UpdateExternalManyToOne[_, T] => PrimaryKeysValues // return the primary keys

	private[mapperdao] val manyToOneOnInsertMap = new MapWithDefault[ColumnInfoManyToOne[_, _, T], OnInsertManyToOne]("onInsertManyToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] val manyToOneOnSelectMap = new MapWithDefault[ColumnInfoManyToOne[_, _, T], OnSelectManyToOne]("onSelectManyToOne must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] val manyToOneOnUpdateMap = new MapWithDefault[ColumnInfoManyToOne[_, _, T], OnUpdateManyToOne]("onUpdateManyToOne must be called for External Entity %s".format(getClass.getName))
	def onInsertManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnInsertManyToOne) = lazyActions(() => manyToOneOnInsertMap + (ci, handler))
	def onInsertManyToOne(handler: OnInsertManyToOne) { manyToOneOnInsertMap.default = Some(handler) }
	def onSelectManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnSelectManyToOne) = lazyActions(() => manyToOneOnSelectMap + (ci, handler))
	def onSelectManyToOne(handler: OnSelectManyToOne) { manyToOneOnSelectMap.default = Some(handler) }
	def onUpdateManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnUpdateManyToOne) = lazyActions(() => manyToOneOnUpdateMap + (ci, handler))
	def onUpdateManyToOne(handler: OnUpdateManyToOne) { manyToOneOnUpdateMap.default = Some(handler) }

	/**
	 * support for one-to-many mapping
	 */
	type OnInsertOneToMany = InsertExternalOneToMany[_, T] => Unit
	type OnSelectOneToMany = SelectExternalOneToMany => List[T]
	type OnUpdateOneToMany = UpdateExternalOneToMany[_, T] => Unit
	private[mapperdao] var oneToManyOnInsertMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, T], OnInsertOneToMany]("onInsertOneToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToManyOnSelectMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, T], OnSelectOneToMany]("onSelectOneToMany must be called for External Entity %s".format(getClass.getName))
	private[mapperdao] var oneToManyOnUpdateMap = new MapWithDefault[ColumnInfoTraversableOneToMany[_, _, T], OnUpdateOneToMany]("onUpdateOneToMany must be called for External Entity %s".format(getClass.getName))

	def onInsertOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnInsertOneToMany) = lazyActions(() => oneToManyOnInsertMap + (ci, handler))
	def onInsertOneToMany(handler: OnInsertOneToMany) = oneToManyOnInsertMap.default = Some(handler)
	def onSelectOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnSelectOneToMany) = lazyActions(() => oneToManyOnSelectMap + (ci, handler))
	def onSelectOneToMany(handler: OnSelectOneToMany) = oneToManyOnSelectMap.default = Some(handler)
	def onUpdateOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnUpdateOneToMany) = lazyActions(() => oneToManyOnUpdateMap + (ci, handler))
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

case class InsertExternalOneToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalOneToOne[T, F](selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)

case class InsertExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalManyToOne[T, F](selectConfig: SelectConfig, primaryKeys: List[Any])
case class UpdateExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)

case class InsertExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, many: Traversable[F])
case class SelectExternalOneToMany(selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, added: Traversable[F], intersection: Traversable[F], removed: Traversable[F])
