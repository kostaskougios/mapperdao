package com.googlecode.mapperdao.jdbc

import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.PreparedStatementCallback
import java.sql.PreparedStatement
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.PreparedStatementCreator
import java.sql.Connection
import java.sql.Statement
import scala.collection.mutable.ArrayBuffer
import java.sql.ResultSet

/**
 * @author kostantinos.kougios
 *
 * Nov 14, 2012
 */
class BatchUtils {
	def batchUpdate(jdbc: JdbcOperations, sql: String, pss: BatchPreparedStatementSetter) = {
		jdbc.execute(new PreparedStatementCreator {
			override def createPreparedStatement(con: Connection) = {
				con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
			}
		}, new PreparedStatementCallback[BatchResult] {
			override def doInPreparedStatement(ps: PreparedStatement) = {
				val batchSize = pss.getBatchSize
				if (JdbcUtils.supportsBatchUpdates(ps.getConnection)) {
					for (i <- 0 until batchSize) {
						pss.setValues(ps, i)
						ps.addBatch()
					}
					val result = ps.executeBatch
					val generatedKeys = ps.getGeneratedKeys
					val rm = new ColumnMapRowMapper

					var idx = 0
					val keys = new Array[java.util.Map[String, Object]](result.length)
					while (generatedKeys.next) {
						keys(idx) = rm.mapRow(generatedKeys, idx)
						idx += 1
					}
					generatedKeys.close()
					BatchResult(result, keys)
				} else throw new IllegalStateException("batch updates not supported by this jdbc driver")
				//					List<Integer> rowsAffected = new ArrayList<Integer>();
				//					for (int i = 0; i < batchSize; i++)
				//					{
				//						pss.setValues(ps, i);
				//						if (ipss != null && ipss.isBatchExhausted(i))
				//						{
				//							break;
				//						}
				//						rowsAffected.add(ps.executeUpdate());
				//					}
				//					int[] rowsAffectedArray = new int[rowsAffected.size()];
				//					for (int i = 0; i < rowsAffectedArray.length; i++)
				//					{
				//						rowsAffectedArray[i] = rowsAffected.get(i);
				//					}
				//					return rowsAffectedArray;
			}
		})
	}
}

case class BatchResult(rowsAffected: Array[Int], keys: Array[java.util.Map[String, Object]])