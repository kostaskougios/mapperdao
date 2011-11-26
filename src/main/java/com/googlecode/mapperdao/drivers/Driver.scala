package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.jdbc.JdbcMap
import com.googlecode.mapperdao.jdbc.UpdateResultWithGeneratedKeys
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.jdbc.UpdateResult
/**
 * all database drivers must implement this trait
 *
 * @author kostantinos.kougios
 *
 * 14 Jul 2011
 */
abstract class Driver {
	val jdbc: Jdbc
	val typeRegistry: TypeRegistry
	/**
	 * =====================================================================================
	 * utility methods
	 * =====================================================================================
	 */
	protected def escapeColumnNames(name: String): String = name
	protected def escapeTableNames(name: String): String = name

	protected[mapperdao] def commaSeparatedListOfSimpleTypeColumns[T](separator: String, columns: Traversable[ColumnBase]): String = columns.map(_.columnName).map(escapeColumnNames _).mkString(separator)
	protected[mapperdao] def commaSeparatedListOfSimpleTypeColumns[T](prefix: String, separator: String, columns: List[ColumnBase]): String = columns.map(_.columnName).map(escapeColumnNames _).mkString(prefix, separator + prefix, "")

	protected[mapperdao] def generateColumnsEqualsValueString(l: List[ColumnBase]): String = generateColumnsEqualsValueString(l, ",\n")

	protected[mapperdao] def generateColumnsEqualsValueString(l: List[ColumnBase], separator: String): String =
		{
			val sb = new StringBuilder(20)
			var cnt = 0
			l.foreach { ci =>
				if (cnt > 0) sb.append(separator) else cnt += 1
				sb append escapeColumnNames(ci.columnName) append "=?"
			}
			sb.toString
		}
	protected[mapperdao] def generateColumnsEqualsValueString(prefix: String, separator: String, l: List[ColumnBase]): String =
		{
			val sb = new StringBuilder(20)
			var cnt = 0
			l.foreach { ci =>
				if (cnt > 0) sb.append(separator) else cnt += 1
				sb append prefix append escapeColumnNames(ci.columnName) append "=?"
			}
			sb.toString
		}

	protected[mapperdao] def getAutoGenerated(ur: UpdateResultWithGeneratedKeys, column: ColumnBase): Any =
		ur.keys.get(column.columnName).get

	/**
	 * =====================================================================================
	 * INSERT
	 * =====================================================================================
	 */

	/**
	 * default implementation of insert, should do for most subclasses
	 */
	def doInsert[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): UpdateResultWithGeneratedKeys =
		{
			val sql = insertSql(tpe, args)
			val a = args.map(_._2)

			val agColumns = tpe.table.autoGeneratedColumns.map(_.columnName).toArray
			if (agColumns.isEmpty) {
				val ur = jdbc.update(sql, a)
				new UpdateResultWithGeneratedKeys(ur.rowsAffected, Map())
			} else {
				jdbc.updateGetAutoGenerated(sql, agColumns, a)
			}
		}

	protected def sequenceSelectNextSql(sequenceColumn: ColumnBase): String = throw new IllegalStateException("Please implement")
	/**
	 * default impl of the insert statement generation
	 */
	protected def insertSql[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)]): String =
		{
			val sb = new StringBuilder(100, "insert into ")
			sb append escapeTableNames(tpe.table.name)

			val sequenceColumns = tpe.table.simpleTypeSequenceColumns
			if (!args.isEmpty || !sequenceColumns.isEmpty) {
				sb append "("
				// append sequences
				// and normal columns
				if (!args.isEmpty || !sequenceColumns.isEmpty) sb append commaSeparatedListOfSimpleTypeColumns(",", sequenceColumns ::: args.map(_._1))
				sb append ")\n"
				sb append "values("
				// sequence values
				if (!sequenceColumns.isEmpty) {
					sb append sequenceColumns.map { sequenceSelectNextSql _ }.mkString(",")
					if (!args.isEmpty) sb append ","
				}
				// column values
				if (!args.isEmpty) sb append "?" append (",?" * (args.size - 1))
				sb append ")"
			}
			sb.toString
		}

	def doInsertManyToMany[PC, T, FPC, F](tpe: Type[PC, T], manyToMany: ManyToMany[FPC, F], left: List[(ColumnBase, Any)], right: List[(ColumnBase, Any)]): Unit =
		{
			val sql = insertManyToManySql(tpe, manyToMany, left, right)
			jdbc.update(sql, left.map(_._2) ::: right.map(_._2))
		}

	protected def insertManyToManySql[PC, T, FPC, F](tpe: Type[PC, T], manyToMany: ManyToMany[FPC, F], left: List[(ColumnBase, Any)], right: List[(ColumnBase, Any)]): String =
		{
			val sb = new StringBuilder(100, "insert into ")
			val linkTable = manyToMany.linkTable
			sb append escapeTableNames(linkTable.name) append "(" append commaSeparatedListOfSimpleTypeColumns(",", linkTable.left) append "," append commaSeparatedListOfSimpleTypeColumns(",", linkTable.right) append ")\n"
			sb append "values(?" append (",?" * (linkTable.left.size - 1 + linkTable.right.size)) append ")"
			sb.toString
		}
	/**
	 * =====================================================================================
	 * UPDATE
	 * =====================================================================================
	 */
	/**
	 * default implementation of update, should do for most subclasses
	 */
	def doUpdate[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): UpdateResult =
		{
			val sql = updateSql(tpe, args, pkArgs)
			jdbc.update(sql, args.map(_._2) ::: pkArgs.map(_._2))
		}
	/**
	 * default impl of the insert statement generation
	 */
	protected def updateSql[PC, T](tpe: Type[PC, T], args: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): String =
		{
			val sb = new StringBuilder(100, "update ")
			sb append escapeTableNames(tpe.table.name) append "\n"
			sb append "set " append generateColumnsEqualsValueString(args.map(_._1))
			sb append "\nwhere " append generateColumnsEqualsValueString(pkArgs.map(_._1), " and ")
			sb.toString
		}

	/**
	 * links one-to-many objects to their parent
	 */
	def doUpdateOneToManyRef[PC, T](tpe: Type[PC, T], foreignKeys: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): UpdateResult =
		{
			val sql = updateOneToManyRefSql(tpe, foreignKeys, pkArgs)
			jdbc.update(sql, foreignKeys.map(_._2) ::: pkArgs.map(_._2))
		}

	protected def updateOneToManyRefSql[PC, T](tpe: Type[PC, T], foreignKeys: List[(ColumnBase, Any)], pkArgs: List[(ColumnBase, Any)]): String =
		{
			val sb = new StringBuilder(100, "update ")
			sb append escapeTableNames(tpe.table.name) append "\n"
			sb append "set " append generateColumnsEqualsValueString(foreignKeys.map(_._1))
			sb append "\nwhere " append generateColumnsEqualsValueString(pkArgs.map(_._1))
			sb.toString
		}

	/**
	 * delete many-to-many rows from link table
	 */
	def doDeleteManyToManyRef[PC, T, PR, R](tpe: Type[PC, T], ftpe: Type[PR, R], manyToMany: ManyToMany[_, _], leftKeyValues: List[(ColumnBase, Any)], rightKeyValues: List[(ColumnBase, Any)]): UpdateResult =
		{
			val sql = deleteManyToManyRefSql(tpe, ftpe, manyToMany, leftKeyValues, rightKeyValues)
			jdbc.update(sql, leftKeyValues.map(_._2) ::: rightKeyValues.map(_._2))
		}
	protected def deleteManyToManyRefSql[PC, T, PR, R](tpe: Type[PC, T], ftpe: Type[PR, R], manyToMany: ManyToMany[_, _], leftKeyValues: List[(ColumnBase, Any)], rightKeyValues: List[(ColumnBase, Any)]): String =
		{
			val sb = new StringBuilder(100, "delete from ")
			sb append escapeTableNames(manyToMany.linkTable.name) append "\nwhere "
			sb append generateColumnsEqualsValueString("", " and ", leftKeyValues.map(_._1) ::: rightKeyValues.map(_._1))
			sb.toString
		}

	def doDeleteAllManyToManyRef[PC, T](tpe: Type[PC, T], manyToMany: ManyToMany[_, _], fkKeyValues: List[Any]): UpdateResult = {
		val sql = deleteAllManyToManyRef(tpe, manyToMany, fkKeyValues)
		jdbc.update(sql, fkKeyValues)
	}
	protected def deleteAllManyToManyRef[PC, T](tpe: Type[PC, T], manyToMany: ManyToMany[_, _], fkKeyValues: List[Any]): String = {
		val sb = new StringBuilder(50, "delete from ")
		sb append escapeTableNames(manyToMany.linkTable.name) append "\nwhere "
		sb append generateColumnsEqualsValueString("", " and ", manyToMany.linkTable.left)
		sb.toString
	}
	/**
	 * =====================================================================================
	 * SELECT
	 * =====================================================================================
	 */
	def selectColumns[PC, T](tpe: Type[PC, T]): List[ColumnBase] =
		{
			val table = tpe.table
			table.simpleTypeColumns ::: table.manyToOneColumns.map(_.columns).flatten ::: table.oneToOneColumns.map(_.selfColumns).flatten
		}
	/**
	 * default impl of select
	 */
	def doSelect[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): List[JdbcMap] =
		{
			val sql = selectSql(tpe, where)

			// 1st step is to get the simple values
			// of this object from the database
			jdbc.queryForList(sql, where.map(_._2))
		}

	protected def selectSql[PC, T](tpe: Type[PC, T], where: List[(SimpleColumn, Any)]): String =
		{
			val columns = selectColumns(tpe)
			val sb = new StringBuilder(100, "select ")
			sb append commaSeparatedListOfSimpleTypeColumns(",", (columns ::: tpe.table.unusedPKs).toSet)
			sb append " from " append escapeTableNames(tpe.table.name)
			sb append "\nwhere " append generateColumnsEqualsValueString(where.map(_._1), " and ")

			sb.toString
		}

	def doSelectManyToMany[PC, T, FPC, F](tpe: Type[PC, T], ftpe: Type[FPC, F], manyToMany: ManyToMany[FPC, F], leftKeyValues: List[(SimpleColumn, Any)]): List[JdbcMap] =
		{
			val sql = selectManyToManySql(tpe, ftpe, manyToMany, leftKeyValues)
			jdbc.queryForList(sql, leftKeyValues.map(_._2))
		}

	protected def selectManyToManySql[PC, T, FPC, F](tpe: Type[PC, T], ftpe: Type[FPC, F], manyToMany: ManyToMany[FPC, F], leftKeyValues: List[(SimpleColumn, Any)]): String =
		{
			val ftable = ftpe.table
			val linkTable = manyToMany.linkTable
			val sb = new StringBuilder(100, "select ")
			val fColumns = selectColumns(ftpe)
			sb append commaSeparatedListOfSimpleTypeColumns(",", fColumns) append "\nfrom " append escapeTableNames(ftpe.table.name) append " f\n"
			sb append "inner join " append escapeTableNames(linkTable.name) append " l on "
			var i = 0
			ftable.primaryKeys.zip(linkTable.right).foreach { z =>
				val PK(left) = z._1
				val right = z._2
				if (i > 0) sb append " and "
				sb append "f." append left.columnName append "=l." append right.name
				i += 1
			}
			sb append "\nwhere " append generateColumnsEqualsValueString("l.", " and ", leftKeyValues.map(_._1))
			sb.toString
		}
	/**
	 * =====================================================================================
	 * DELETE
	 * =====================================================================================
	 */
	def doDelete[PC, T](tpe: Type[PC, T], whereColumnValues: List[(SimpleColumn, Any)]): Unit =
		{
			val sql = deleteSql(tpe, whereColumnValues)
			jdbc.update(sql, whereColumnValues.map(_._2))
		}

	protected def deleteSql[PC, T](tpe: Type[PC, T], whereColumnValues: List[(SimpleColumn, Any)]): String =
		{
			val sb = new StringBuilder(100, "delete from ")
			sb append escapeTableNames(tpe.table.name) append " where " append generateColumnsEqualsValueString(whereColumnValues.map(_._1), " and ")

			sb.toString
		}

	def doDeleteOneToOneReverse[PC, T, FPC, FT](tpe: Type[PC, T], ftpe: Type[FPC, FT], oneToOneReverse: OneToOneReverse[FPC, FT], keyValues: List[Any]): Unit =
		{
			val sql = deleteOneToOneReverseSql(tpe, ftpe, oneToOneReverse)
			jdbc.update(sql, keyValues)
		}

	def deleteOneToOneReverseSql[PC, T, FPC, FT](tpe: Type[PC, T], ftpe: Type[FPC, FT], oneToOneReverse: OneToOneReverse[FPC, FT]): String =
		{
			val sb = new StringBuilder(100, "delete from ")
			sb append escapeTableNames(ftpe.table.name) append " where " append generateColumnsEqualsValueString(oneToOneReverse.foreignColumns, " and ")

			sb.toString
		}
	/**
	 * =====================================================================================
	 * QUERIES
	 * =====================================================================================
	 */

	// select ... from 
	def startQuery[PC, T](queryConfig: QueryConfig, aliases: QueryDao.Aliases, qe: Query.QueryEntity[PC, T], columns: List[ColumnBase]): String =
		{
			val entity = qe.entity
			val tpe = entity.tpe
			val sb = new StringBuilder(100, "select ")
			val qAS = queryAfterSelect(queryConfig, aliases, qe, columns)
			if (!qAS.isEmpty) {
				sb.append(qAS).append(',')
			}
			val alias = aliases(entity)
			sb append commaSeparatedListOfSimpleTypeColumns(alias + ".", ",", columns)
			sb append "\nfrom " append escapeTableNames(tpe.table.name) append " " append alias

			sb.toString
		}

	def queryAfterSelect[PC, T](queryConfig: QueryConfig, aliases: QueryDao.Aliases, qe: Query.QueryEntity[PC, T], columns: List[ColumnBase]): String = ""

	// creates the join for one-to-one-reverse
	def oneToOneReverseJoin(aliases: QueryDao.Aliases, joinEntity: Entity[_, _], foreignEntity: Entity[_, _], oneToOneReverse: OneToOneReverse[_, _]): String =
		{
			val tpe = joinEntity.tpe
			val table = tpe.table
			val foreignTpe = foreignEntity.tpe
			val foreignTable = foreignTpe.table
			val fAlias = aliases(foreignEntity)
			val jAlias = aliases(joinEntity)

			val sb = new StringBuilder
			sb append "\njoin " append escapeTableNames(foreignTable.name) append " " append fAlias append " on "
			(table.primaryKeys zip oneToOneReverse.foreignColumns).foreach { t =>
				sb append jAlias append "." append t._1.columnName append " = " append fAlias append "." append t._2.columnName append " "
			}
			sb.toString
		}

	// creates the join for many-to-one
	def manyToOneJoin(aliases: QueryDao.Aliases, joinEntity: Entity[_, _], foreignEntity: Entity[_, _], manyToOne: ManyToOne[_, _]): String =
		{
			val foreignTpe = foreignEntity.tpe
			val foreignTable = foreignTpe.table
			val fAlias = aliases(foreignEntity)
			val jAlias = aliases(joinEntity)

			val sb = new StringBuilder
			sb append "\njoin " append escapeTableNames(foreignTable.name) append " " append fAlias append " on "
			(manyToOne.columns zip foreignTable.primaryKeys).foreach { t =>
				sb append jAlias append "." append t._1.columnName append " = " append fAlias append "." append t._2.columnName append " "
			}
			sb.toString
		}

	// creates the join for one-to-many
	def oneToManyJoin(aliases: QueryDao.Aliases, joinEntity: Entity[_, _], foreignEntity: Entity[_, _], oneToMany: OneToMany[_, _]): String =
		{
			val joinTpe = joinEntity.tpe
			val foreignTpe = foreignEntity.tpe

			val foreignTable = foreignTpe.table
			val fAlias = aliases(foreignEntity)
			val jAlias = aliases(joinEntity)

			val sb = new StringBuilder
			sb append "\njoin " append escapeTableNames(foreignTable.name) append " " append fAlias append " on "
			(joinTpe.table.primaryKeys zip oneToMany.foreignColumns).foreach { t =>
				sb append jAlias append "." append t._1.columnName append " = " append fAlias append "." append t._2.columnName append " "
			}
			sb.toString
		}
	// creates the join for one-to-many
	def manyToManyJoin(aliases: QueryDao.Aliases, joinEntity: Entity[_, _], foreignEntity: Entity[_, _], manyToMany: ManyToMany[_, _]): String =
		{
			val joinTpe = joinEntity.tpe
			val foreignTpe = foreignEntity.tpe

			val foreignTable = foreignTpe.table
			val fAlias = aliases(foreignEntity)
			val jAlias = aliases(joinEntity)

			val linkTable = manyToMany.linkTable
			val linkTableAlias = aliases(linkTable)

			val sb = new StringBuilder
			// left part
			sb append "\njoin " append escapeTableNames(linkTable.name) append " " append linkTableAlias append " on "
			(joinTpe.table.primaryKeys zip linkTable.left).foreach { t =>
				sb append linkTableAlias append "." append t._2.columnName append " = " append jAlias append "." append t._1.columnName append " "
			}

			// right part
			sb append "\njoin " append escapeTableNames(foreignTable.name) append " " append fAlias append " on "
			(foreignTable.primaryKeys zip linkTable.right).foreach { t =>
				sb append fAlias append "." append t._1.columnName append " = " append linkTableAlias append "." append t._2.columnName append " "
			}
			sb.toString
		}

	// creates the join sql and params for joins (including join on expressions, i.e. join T on j1.name<>j2.name)
	def joinTable(aliases: QueryDao.Aliases, join: Query.Join[_, _, Entity[_, _], _, _]): (String, List[Any]) =
		{
			val jEntity = join.entity
			val jTable = jEntity.tpe.table
			val qAlias = aliases(jEntity)
			val sb = new StringBuilder
			sb append "\njoin " append escapeTableNames(jTable.name) append " " append qAlias

			var args = if (join.on != null) {
				val expressions = queryExpressions(aliases, join.on.ons, sb)
				sb append " on " append expressions._1
				expressions._2
			} else List[Any]()

			(sb.toString, args)
		}

	// creates the sql and params for expressions (i.e. id=5 and name='x')
	def queryExpressions[PC, T](aliases: QueryDao.Aliases, wheres: List[Query.QueryExpressions[PC, T]], joinsSb: StringBuilder): (String, List[Any]) =
		{
			val sb = new StringBuilder(100)
			var args = List.newBuilder[Any]
			wheres.map(_.clauses).foreach { op =>
				def inner(op: OpBase): Unit = op match {
					case o: Operation[_] =>
						sb append resolveWhereExpression(aliases, args, o.left)
						sb append ' ' append o.operand.sql append ' ' append resolveWhereExpression(aliases, args, o.right)
					case and: AndOp =>
						sb append "( "
						inner(and.left)
						sb append " and "
						inner(and.right)
						sb append " )"
					case and: OrOp =>
						sb append "( "
						inner(and.left)
						sb append " or "
						inner(and.right)
						sb append " )"
					case mto: ManyToOneOperation[Any, Any, Any] =>
						val ManyToOneOperation(left, operand, right) = mto
						if (right == null) {
							left.columns foreach { c =>
								sb append resolveWhereExpression(aliases, args, c)
								operand match {
									case EQ() => sb append " is null"
									case NE() => sb append " is not null"
									case _ => throw new IllegalArgumentException("operand %s not valid when right hand parameter is null.".format(operand))
								}
							}
						} else {
							val fTpe = left.foreign.entity.tpe
							val fPKs = fTpe.table.toListOfPrimaryKeyValues(right)
							if (left.columns.size != fPKs.size) throw new IllegalStateException("foreign keys %s don't match foreign key columns %s".format(fPKs, left.columns))
							left.columns zip fPKs foreach { t =>
								sb append resolveWhereExpression(aliases, args, t._1)
								sb append ' ' append operand.sql append ' ' append resolveWhereExpression(aliases, args, t._2)
							}
						}
					case OneToManyOperation(left: OneToMany[_, _], operand: Operand, right: Any) =>
						val entity = typeRegistry.entityOf(left)
						val foreignEntity = left.foreign.entity
						joinsSb append oneToManyJoin(aliases, entity, foreignEntity, left)
						val fTpe = foreignEntity.tpe
						val fPKColumnAndValues = fTpe.table.toListOfPrimaryKeyAndValueTuples(right)
						fPKColumnAndValues.foreach { t =>
							sb append resolveWhereExpression(aliases, args, t._1)
							sb append ' ' append operand.sql append ' ' append resolveWhereExpression(aliases, args, t._2)
						}
					case ManyToManyOperation(left: ManyToMany[_, _], operand: Operand, right: Any) =>
						val entity = typeRegistry.entityOf(left)
						val foreignEntity = left.foreign.entity
						joinsSb append manyToManyJoin(aliases, entity, foreignEntity, left)
						val fTpe = foreignEntity.tpe
						val fPKColumnAndValues = fTpe.table.toListOfPrimaryKeyAndValueTuples(right)
						fPKColumnAndValues.foreach { t =>
							sb append resolveWhereExpression(aliases, args, t._1)
							sb append ' ' append operand.sql append ' ' append resolveWhereExpression(aliases, args, t._2)
						}
					//					case OneToOneReverseOperation(left: OneToOneReverse[_], operand: Operand, right: Any) =>
				}

				inner(op)
			}
			(sb.toString, args.result)
		}

	protected def resolveWhereExpression(aliases: QueryDao.Aliases, args: scala.collection.mutable.Builder[Any, List[Any]], v: Any): String = v match {
		case c: ColumnBase =>
			aliases(c) + "." + escapeColumnNames(c.columnName)
		case _ =>
			args += v
			"?"
	}

	// create order by clause
	def orderBy(queryConfig: QueryConfig, aliases: QueryDao.Aliases, columns: List[(ColumnBase, Query.AscDesc)]): String = if (shouldCreateOrderByClause(queryConfig)) {
		"\norder by " + columns.map { caq =>
			val c = caq._1
			val ascDesc = caq._2
			aliases(c) + "." + escapeColumnNames(c.columnName) + " " + ascDesc.sql
		}.mkString(",")
	} else ""

	def shouldCreateOrderByClause(queryConfig: QueryConfig): Boolean = true

	// called at the start of each query sql generation, sql is empty at this point
	def beforeStartOfQuery[PC, T](queryConfig: QueryConfig, qe: Query.QueryEntity[PC, T], columns: List[ColumnBase], sql: StringBuilder): Unit =
		{
		}
	// called at the end of each query sql generation
	def endOfQuery[PC, T](queryConfig: QueryConfig, qe: Query.QueryEntity[PC, T], sql: StringBuilder): Unit =
		{
		}

	/**
	 * =====================================================================================
	 * aggregate methods
	 * =====================================================================================
	 */
	def countSql[PC, T](aliases: QueryDao.Aliases, entity: Entity[PC, T]): String =
		{
			val tpe = entity.tpe
			val sb = new StringBuilder(50, "select count(*)")
			val alias = aliases(entity)
			sb append "\nfrom " append escapeTableNames(tpe.table.name) append " " append alias
			sb.toString
		}
	/**
	 * =====================================================================================
	 * standard methods
	 * =====================================================================================
	 */
	override def toString = "Driver(%s,%s)".format(jdbc, typeRegistry)
}