package com.googlecode.mapperdao.jdbc

import javax.sql.DataSource
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import org.springframework.jdbc.core.ColumnMapRowMapper
import scala.collection.JavaConversions._
import org.springframework.jdbc.core.ResultSetExtractor
import scala.collection.mutable.Builder
import org.springframework.jdbc.core.ArgPreparedStatementSetter
import java.sql.Timestamp
import java.util.Calendar
import java.util.GregorianCalendar
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import com.googlecode.mapperdao.TypeManager
import org.springframework.jdbc.core.PreparedStatementCreatorFactory
import org.springframework.jdbc.support.KeyHolder
import org.springframework.jdbc.core.PreparedStatementCreator
import java.sql.PreparedStatement
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Connection
import java.sql.Statement
import org.springframework.jdbc.core.StatementCreatorUtils
import org.springframework.jdbc.core.SqlTypeValue
import org.springframework.jdbc.core.JdbcTemplate
import org.joda.time.DateTime
import org.joda.time.Chronology
import java.sql.Types
import org.springframework.jdbc.core.SqlParameterValue

/**
 * scal-ified JdbcTemplate
 *
 * This provides access to the database via spring framework's JdbcTemplate class
 *
 * @author kostantinos.kougios
 *
 * 11 Jul 2011
 */
class Jdbc private (val dataSource: DataSource, val chronology: Chronology) {
	if (dataSource == null) throw new NullPointerException("dataSource shouldn't be null")

	private val j = new JdbcTemplate(dataSource)
	private val logger: Logger = LoggerFactory.getLogger(getClass)
	private val isDebugEnabled = logger.isDebugEnabled
	private def seq(args: Any*) = args.toSeq.asInstanceOf[Seq[AnyRef]]

	/**
	 * converts a query and it's arguments to a string, useful for debugging & logging
	 */
	def toString(sql: String, args: Seq[_]) =
		{
			var counter = 0
			sql.map {
				case '?' =>
					if (counter >= args.length) {
						"<out of bounds>"
					} else {
						val a = args(counter) match {
							case spv: SqlParameterValue =>
								spv.getValue
							case x => x
						}
						counter += 1
						a match {
							case s: String => "'" + s + "'"
							case c: Calendar => "'" + c.getTime + "'"
							case _ => a
						}
					}
				case c => c
			}.mkString
		}

	def query(sql: String, mapper: java.util.Map[String, _] => Boolean, args: Array[_], maxResults: Int = Int.MaxValue) {
		if (isDebugEnabled) {
			logger.debug("sql:\n" + toString(sql, args))
		}
		val rse = new ResultSetExtractor[Unit]() {
			override def extractData(rs: ResultSet) {
				val rm = new ColumnMapRowMapper
				var i = 0
				while (rs.next && i < maxResults) {
					val m = rm.mapRow(rs, i)

					// convert types using the typeManager
					val it = m.entrySet.iterator
					while (it.hasNext) {
						val e = it.next
						e.setValue(convert(e.getValue).asInstanceOf[Object])
					}
					if (mapper(m)) i += 1; else i = maxResults // exit if false
				}
			}
		}
		if (args.length == 0) {
			j.query(sql, rse)
		} else {
			val rargs = args.map(reverseConvert _)
			j.query(sql, rse, rargs.asInstanceOf[Array[AnyRef]]: _*)
		}
	}

	def queryForList(sql: String, args: List[Any]): List[JdbcMap] = queryForList(sql, args: _*)
	def queryForList(sql: String, args: Any*): List[JdbcMap] =
		{
			val builder = List.newBuilder[JdbcMap]
			val a = args.toArray
			query(sql, { m =>
				builder += new JdbcMap(m)
				true
			}, a)
			builder.result
		}

	def queryForInt(sql: String, args: List[Any]): Int = queryForInt(sql, args: _*)
	def queryForInt(sql: String, args: Any*): Int = j.queryForInt(sql, args.asInstanceOf[Seq[Object]]: _*)
	def queryForLong(sql: String, args: List[Any]): Long = queryForLong(sql, args: _*)
	def queryForLong(sql: String, args: Any*): Long = {
		if (isDebugEnabled) {
			logger.debug("sql:\n" + toString(sql, args))
		}
		j.queryForLong(sql, args.asInstanceOf[Seq[Object]]: _*)
	}

	/**
	 * query that expect 0 or 1 row to be returned.
	 * for 0 rows, it returns None
	 * for 1 row, it returns an Option[Map[String, _]]
	 * for more than 1 row, it throws an IllegalStateException as this state is not expected
	 */
	def queryForMap(sql: String, args: List[Any]): Option[JdbcMap] = queryForMap(sql, args: _*)

	/**
	 * query that expect 0 or 1 row to be returned.
	 * for 0 rows, it returns None
	 * for 1 row, it returns an Option[Map[String, _]]
	 * for more than 1 row, it throws an IllegalStateException as this state is not expected
	 */
	def queryForMap(sql: String, args: Any*): Option[JdbcMap] =
		{
			val l = queryForList(sql, args: _*)
			if (l.isEmpty) None; else if (l.size != 1) throw new IllegalStateException("more than one results found for " + sql) else Some(l.get(0))
		}

	def update(sql: String, args: List[Any]): UpdateResult = update(sql, args: _*)

	def update(sql: String, args: Any*): UpdateResult =
		{
			if (isDebugEnabled) {
				logger.debug("sql:\n" + toString(sql, args))
			}
			if (args.length == 0) {
				val rowsAffected = j.update(sql)
				new UpdateResult(rowsAffected)
			} else {
				val a = args.map(v => reverseConvert(v)).asInstanceOf[Seq[AnyRef]]
				val rowsAffected = j.update(sql, a: _*)
				new UpdateResult(rowsAffected)
			}
		}

	def updateGetAutoGenerated(sql: String, args: List[Any]): UpdateResultWithGeneratedKeys = updateGetAutoGenerated(sql, args: _*)
	def updateGetAutoGenerated(sql: String, args: Any*): UpdateResultWithGeneratedKeys =
		{
			if (isDebugEnabled) {
				logger.debug("sql:\n" + toString(sql, args))
			}
			val keyHolder: KeyHolder = new GeneratedKeyHolder
			val rowsAffected = j.update(new PreparedStatementCreator {
				override def createPreparedStatement(con: Connection): PreparedStatement =
					{
						val ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
						var idx = 1
						args.foreach { arg =>
							StatementCreatorUtils.setParameterValue(ps, idx, SqlTypeValue.TYPE_UNKNOWN, reverseConvert(arg))
							idx += 1
						}
						ps
					}
			}, keyHolder);
			val keysM = keyHolder.getKeys
			val keys = if (keysM == null) Map[String, Any]() else keysM.toMap
			new UpdateResultWithGeneratedKeys(rowsAffected, keys)
		}

	def updateGetAutoGenerated(sql: String, autogeneratedColumns: Array[String], args: List[Any]): UpdateResultWithGeneratedKeys = updateGetAutoGenerated(sql, autogeneratedColumns, args: _*)
	def updateGetAutoGenerated(sql: String, autogeneratedColumns: Array[String], args: Any*): UpdateResultWithGeneratedKeys =
		{
			if (isDebugEnabled) {
				logger.debug("sql:\n" + toString(sql, args))
			}
			val keyHolder: KeyHolder = new GeneratedKeyHolder
			val rowsAffected = j.update(new PreparedStatementCreator {
				override def createPreparedStatement(con: Connection): PreparedStatement =
					{
						val ps = con.prepareStatement(sql, autogeneratedColumns)
						var idx = 1
						args.foreach { arg =>
							StatementCreatorUtils.setParameterValue(ps, idx, SqlTypeValue.TYPE_UNKNOWN, reverseConvert(arg))
							idx += 1
						}
						ps
					}
			}, keyHolder);
			val keysM = keyHolder.getKeys
			val keys = if (keysM == null) Map[String, Any]() else keysM.toMap
			new UpdateResultWithGeneratedKeys(rowsAffected, keys)
		}

	override def toString = "Jdbc(%s)".format(dataSource)

	private def convert(o: Any): Any = o match {
		case t: java.util.Date => new DateTime(t, chronology)
		case c: java.util.Calendar => new DateTime(c, chronology)
		case _ => o
	}

	private def reverseConvert(o: Any): Any = {
		def rc(o: Any) = o match {
			case t: DateTime => t.toCalendar(null)
			case d: BigDecimal => d.bigDecimal
			case i: BigInt => i.bigInteger
			case _ => o
		}

		o match {
			case spv: SqlParameterValue =>
				val v = rc(spv.getValue)
				new SqlParameterValue(spv.getSqlType, v)
			case o => rc(o)
		}
	}

}

object Jdbc {
	def apply(dataSource: DataSource, chronology: Chronology) = new Jdbc(dataSource, chronology)

	private val sqlParamMap: Map[Class[_], Int] = Map(
		classOf[String] -> Types.VARCHAR,
		classOf[Calendar] -> Types.TIMESTAMP,
		classOf[BigDecimal] -> Types.NUMERIC,
		classOf[Boolean] -> Types.BOOLEAN,
		classOf[Byte] -> Types.SMALLINT,
		classOf[Short] -> Types.SMALLINT,
		classOf[Int] -> Types.INTEGER,
		classOf[Long] -> Types.BIGINT,
		classOf[Float] -> Types.FLOAT,
		classOf[Double] -> Types.DOUBLE,
		classOf[DateTime] -> Types.TIMESTAMP
	)

	def toSqlParameter(l: List[(Class[_], Any)]): List[SqlParameterValue] = l.map {
		case (clz, v) =>
			val tpe = sqlParamMap(clz)
			new SqlParameterValue(tpe, v)
	}
}