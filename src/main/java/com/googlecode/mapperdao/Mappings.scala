package com.googlecode.mapperdao

/**
 * mapping tables to entities
 *
 * @author kostantinos.kougios
 *
 * 12 Jul 2011
 */

protected abstract class ColumnBase {
	def columnName: String
	def alias: String

	def isAutoGenerated: Boolean
	def isSequence: Boolean
}

case class Table[PC, T](name: String, columnInfosPlain: List[ColumnInfoBase[T, _]], extraColumnInfosPersisted: List[ColumnInfoBase[T with PC, _]], val unusedPKs: List[SimpleColumn]) {

	val columns: List[ColumnBase] = columnInfosPlain.map(_.column) ::: extraColumnInfosPersisted.map(_.column)
	// the primary keys for this table
	val primaryKeys: List[PK] = columns.collect {
		case pk: PK => pk
	}
	val primaryKeyColumns: List[SimpleColumn] = primaryKeys.map(_.column)

	val autoGeneratedColumns: List[ColumnBase] = columns.filter(_.isAutoGenerated)
	val columnsWithoutAutoGenerated: List[ColumnBase] = columns.filterNot(_.isAutoGenerated)

	val simpleTypeColumns: List[ColumnBase] = columns.collect {
		case c: Column => c
		case pk: PK => pk
	}

	val simpleTypeSequenceColumns: List[ColumnBase] = simpleTypeColumns.filter(_.isSequence)
	val simpleTypeAutoGeneratedColumns: List[ColumnBase] = simpleTypeColumns.filter(_.isAutoGenerated)
	val simpleTypeNotAutoGeneratedColumns: List[ColumnBase] = simpleTypeColumns.filterNot(_.isAutoGenerated)

	val simpleTypeColumnInfos = columnInfosPlain.collect {
		case ci: ColumnInfo[T, _] => ci
	}

	val oneToOneColumns: List[OneToOne[Any]] = columns.collect {
		case c: OneToOne[Any] => c
	}

	val oneToOneReverseColumns: List[OneToOneReverse[Any]] = columns.collect {
		case c: OneToOneReverse[Any] => c
	}

	val oneToManyColumns: List[OneToMany[Any]] = columns.collect {
		case c: OneToMany[Any] => c
	}
	val manyToOneColumns: List[ManyToOne[Any]] = columns.collect {
		case mto: ManyToOne[Any] => mto
	}
	val manyToOneColumnsFlattened: List[Column] = columns.collect {
		case ManyToOne(columns: List[Column], _) => columns
	}.flatten

	val manyToManyColumns: List[ManyToMany[Any]] = columns.collect {
		case c: ManyToMany[Any] => c
	}

	val oneToOneColumnInfos: List[ColumnInfoOneToOne[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoOneToOne[T, _] => c
	}
	val oneToOneReverseColumnInfos: List[ColumnInfoOneToOneReverse[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoOneToOneReverse[T, _] => c
	}

	val oneToManyColumnInfos: List[ColumnInfoTraversableOneToMany[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoTraversableOneToMany[T, _] => c
	}
	val manyToOneColumnInfos: List[ColumnInfoManyToOne[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoManyToOne[T, _] => c
	}
	val manyToManyColumnInfos: List[ColumnInfoTraversableManyToMany[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoTraversableManyToMany[T, _] => c
	}

	val columnToColumnInfoMap: Map[ColumnBase, ColumnInfoBase[T, _]] = columnInfosPlain.map(ci => (ci.column, ci)).toMap
	val pcColumnToColumnInfoMap: Map[ColumnBase, ColumnInfoBase[T with PC, _]] = extraColumnInfosPersisted.map(ci => (ci.column, ci)).toMap

	val manyToManyToColumnInfoMap: Map[ColumnBase, ColumnInfoTraversableManyToMany[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoTraversableManyToMany[T, _] => (c.column, c)
	}.toMap

	val oneToManyToColumnInfoMap: Map[ColumnBase, ColumnInfoTraversableOneToMany[T, _]] = columnInfosPlain.collect {
		case c: ColumnInfoTraversableOneToMany[T, _] => (c.column, c)
	}.toMap

	def toListOfPrimaryKeyValues(o: T): List[Any] = toListOfPrimaryKeyAndValueTuples(o).map(_._2)
	def toListOfPrimaryKeyAndValueTuples(o: T): List[(PK, Any)] = toListOfColumnAndValueTuples(primaryKeys, o)
	def toListOfPrimaryKeySimpleColumnAndValueTuples(o: T): List[(SimpleColumn, Any)] = toListOfColumnAndValueTuples(primaryKeys, o)
	def toListOfUnusedPrimaryKeySimpleColumnAndValueTuples(o: T): List[(SimpleColumn, Any)] = toListOfColumnAndValueTuples(unusedPKs, o)

	def toListOfColumnAndValueTuples[CB <: ColumnBase](columns: List[CB], o: T): List[(CB, Any)] = columns.map { c =>
		val ctco = columnToColumnInfoMap.get(c)
		if (ctco.isDefined) {
			if (o == null) (c, null) else (c, ctco.get.columnToValue(o))
		} else {
			o match {
				case pc: T with PC => (c, pcColumnToColumnInfoMap(c).columnToValue(pc))
				case null => (c, null)
			}
		}
	}

	def toColumnAndValueMap(columns: List[ColumnBase], o: T): Map[ColumnBase, Any] = columns.map { c => (c, columnToColumnInfoMap(c).columnToValue(o)) }.toMap
	def toPCColumnAndValueMap(columns: List[ColumnBase], o: T with PC): Map[ColumnBase, Any] = columns.map { c => (c, pcColumnToColumnInfoMap(c).columnToValue(o)) }.toMap

	def toColumnAliasAndValueMap(columns: List[ColumnBase], o: T): Map[String, Any] = toColumnAndValueMap(columns, o).map(e => (e._1.alias, e._2))
	def toPCColumnAliasAndValueMap(columns: List[ColumnBase], o: T with PC): Map[String, Any] = toPCColumnAndValueMap(columns, o).map(e => (e._1.alias, e._2))
}

case class LinkTable(name: String, left: List[Column], right: List[Column])
object LinkTable {
	def apply(name: String, left: String, right: String): LinkTable = LinkTable(name, Column.many(left), Column.many(right))
}

/**
 * Columns
 */
abstract class SimpleColumn extends ColumnBase

case class Column(name: String) extends SimpleColumn {
	def columnName = name
	def alias = name
	def isAutoGenerated = false
	def isSequence = false
}

object Column {
	def many(name: String): List[Column] = List(Column(name))
	def many(name1: String, name2: String): List[Column] = List(Column(name1), Column(name2))
}

case class AutoGenerated(name: String, val sequence: Option[String]) extends SimpleColumn {
	def columnName = name
	def alias = name
	def isAutoGenerated = true
	def isSequence = sequence.isDefined
}
case class PK(column: SimpleColumn) extends SimpleColumn {
	def columnName = column.columnName
	def alias = columnName
	def isAutoGenerated = column.isAutoGenerated
	def isSequence = column.isSequence
}

protected abstract class ColumnRelationshipBase[F](foreign: TypeRef[F]) extends ColumnBase

case class OneToOne[F](foreign: TypeRef[F], selfColumns: List[Column]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("OneToOne doesn't have a columnName")
	def alias = foreign.alias
	def isAutoGenerated = false
	def isSequence = false
}

case class OneToOneReverse[F](foreign: TypeRef[F], foreignColumns: List[Column]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("OneToOneReverse doesn't have a columnName")
	def alias = foreign.alias
	def isAutoGenerated = false
	def isSequence = false
}

case class OneToMany[F](foreign: TypeRef[F], foreignColumns: List[Column]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("OneToMany doesn't have a columnName")
	def alias = foreign.alias
	def isAutoGenerated = false
	def isSequence = false
}

case class ManyToOne[F](columns: List[Column], foreign: TypeRef[F]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("ManyToOne doesn't have a columnName")
	def alias = foreign.alias
	def isAutoGenerated = false
	def isSequence = false
}

case class ManyToMany[F](linkTable: LinkTable, foreign: TypeRef[F]) extends ColumnRelationshipBase(foreign) {
	def columnName = throw new IllegalStateException("ManyToMany doesn't have a columnName")
	def alias = foreign.alias
	def isAutoGenerated = false
	def isSequence = false
}

case class Type[PC, T](val clz: Class[T], val constructor: ValuesMap => T with PC with Persisted, table: Table[PC, T])

case class TypeRef[F](alias: String, clz: Class[F])

/**
 * Column Infos
 */
class ColumnInfoBase[T, V](val column: ColumnBase, val columnToValue: T => V)

case class ColumnInfo[T, V](override val column: SimpleColumn, override val columnToValue: T => V, val dataType: Class[V]) extends ColumnInfoBase[T, V](column, columnToValue)

/**
 * relationship column infos
 */
class ColumnInfoRelationshipBase[T, V, F](override val column: ColumnRelationshipBase[F], override val columnToValue: T => V) extends ColumnInfoBase[T, V](column, columnToValue)

case class ColumnInfoOneToOne[T, F](override val column: OneToOne[F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, F](column, columnToValue)
case class ColumnInfoOneToOneReverse[T, F](override val column: OneToOneReverse[F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, F](column, columnToValue)
case class ColumnInfoTraversableOneToMany[T, F](override val column: OneToMany[F], override val columnToValue: (_ >: T) => Traversable[F]) extends ColumnInfoRelationshipBase[T, Traversable[F], F](column, columnToValue)
case class ColumnInfoManyToOne[T, F](override val column: ManyToOne[F], override val columnToValue: (_ >: T) => F) extends ColumnInfoRelationshipBase[T, F, F](column, columnToValue)
case class ColumnInfoTraversableManyToMany[T, F](override val column: ManyToMany[F], override val columnToValue: T => Traversable[F]) extends ColumnInfoRelationshipBase[T, Traversable[F], F](column, columnToValue)
