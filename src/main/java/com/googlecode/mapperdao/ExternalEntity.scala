package com.googlecode.mapperdao
import com.googlecode.mapperdao.utils.LazyActions

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
	private[mapperdao] var manyToManyOnInsertMap = Map[ColumnInfoTraversableManyToMany[_, _, T], OnInsertManyToMany]()
	private[mapperdao] var manyToManyOnSelectMap = Map[ColumnInfoTraversableManyToMany[_, _, T], OnSelectManyToMany]()
	private[mapperdao] var manyToManyOnUpdateMap = Map[ColumnInfoTraversableManyToMany[_, _, T], OnUpdateManyToMany]()
	def onInsertManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnInsertManyToMany) = lazyActions(() => manyToManyOnInsertMap += (ci -> handler))
	def onSelectManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnSelectManyToMany) = lazyActions(() => manyToManyOnSelectMap += (ci -> handler))
	def onUpdateManyToMany(ci: => ColumnInfoTraversableManyToMany[_, _, T])(handler: OnUpdateManyToMany) = lazyActions(() => manyToManyOnUpdateMap += (ci -> handler))

	/**
	 * support for one-to-one reverse mapping
	 */

	type OnInsertOneToOne = InsertExternalOneToOne[_, T] => Unit
	type OnSelectOneToOne = SelectExternalOneToOne[_, T] => T
	type OnUpdateOneToOne = UpdateExternalOneToOne[_, T] => Unit
	private[mapperdao] var oneToOneOnInsertMap = Map[ColumnInfoOneToOneReverse[_, _, T], OnInsertOneToOne]()
	private[mapperdao] var oneToOneOnSelectMap = Map[ColumnInfoOneToOneReverse[_, _, T], OnSelectOneToOne]()
	private[mapperdao] var oneToOneOnUpdateMap = Map[ColumnInfoOneToOneReverse[_, _, T], OnUpdateOneToOne]()
	def onInsertOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnInsertOneToOne) = lazyActions(() => oneToOneOnInsertMap += (ci -> handler))
	def onSelectOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnSelectOneToOne) = lazyActions(() => oneToOneOnSelectMap += (ci -> handler))
	def onUpdateOneToOne(ci: => ColumnInfoOneToOneReverse[_, _, T])(handler: OnUpdateOneToOne) = lazyActions(() => oneToOneOnUpdateMap += (ci -> handler))

	/**
	 * support for many-to-one mapping
	 */
	type OnInsertManyToOne = InsertExternalManyToOne[_, T] => PrimaryKeysValues // return the primary keys
	type OnSelectManyToOne = SelectExternalManyToOne[_, T] => T // return the actual one-value
	type OnUpdateManyToOne = UpdateExternalManyToOne[_, T] => PrimaryKeysValues // return the primary keys

	private[mapperdao] var manyToOneOnInsertMap = Map[ColumnInfoManyToOne[_, _, T], OnInsertManyToOne]()
	private[mapperdao] var manyToOneOnSelectMap = Map[ColumnInfoManyToOne[_, _, T], OnSelectManyToOne]()
	private[mapperdao] var manyToOneOnUpdateMap = Map[ColumnInfoManyToOne[_, _, T], OnUpdateManyToOne]()
	def onInsertManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnInsertManyToOne) = lazyActions(() => manyToOneOnInsertMap += (ci -> handler))
	def onSelectManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnSelectManyToOne) = lazyActions(() => manyToOneOnSelectMap += (ci -> handler))
	def onUpdateManyToOne(ci: => ColumnInfoManyToOne[_, _, T])(handler: OnUpdateManyToOne) = lazyActions(() => manyToOneOnUpdateMap += (ci -> handler))

	/**
	 * support for one-to-many mapping
	 */
	type OnInsertOneToMany = InsertExternalOneToMany[_, T] => Unit
	type OnSelectOneToMany = SelectExternalOneToMany => List[T]
	type OnUpdateOneToMany = UpdateExternalOneToMany[_, T] => Unit
	private[mapperdao] var oneToManyOnInsertMap = Map[ColumnInfoTraversableOneToMany[_, _, T], OnInsertOneToMany]()
	private[mapperdao] var oneToManyOnSelectMap = Map[ColumnInfoTraversableOneToMany[_, _, T], OnSelectOneToMany]()
	private[mapperdao] var oneToManyOnUpdateMap = Map[ColumnInfoTraversableOneToMany[_, _, T], OnUpdateOneToMany]()

	def onInsertOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnInsertOneToMany) = lazyActions(() => oneToManyOnInsertMap += (ci -> handler))
	def onSelectOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnSelectOneToMany) = lazyActions(() => oneToManyOnSelectMap += (ci -> handler))
	def onUpdateOneToMany(ci: => ColumnInfoTraversableOneToMany[_, _, T])(handler: OnUpdateOneToMany) = lazyActions(() => oneToManyOnUpdateMap += (ci -> handler))

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
case class SelectExternalManyToMany(selectConfig: SelectConfig, foreignIds: List[Any])
object UpdateExternalManyToMany {
	object Operation extends Enumeration {
		val Remove, Add = Value
	}
	type Operation = Operation.Value
}
case class UpdateExternalManyToMany[T, F](updateConfig: UpdateConfig, operation: UpdateExternalManyToMany.Operation, entity: T, foreign: F)

case class InsertExternalOneToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalOneToOne[T, F](selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)

case class InsertExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)
case class SelectExternalManyToOne[T, F](selectConfig: SelectConfig, primaryKeys: List[Any])
case class UpdateExternalManyToOne[T, F](updateConfig: UpdateConfig, entity: T, foreign: F)

case class InsertExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, many: Traversable[F])
case class SelectExternalOneToMany(selectConfig: SelectConfig, foreignIds: List[Any])
case class UpdateExternalOneToMany[T, F](updateConfig: UpdateConfig, entity: T, added: Traversable[F], intersection: Traversable[F], removed: Traversable[F])
