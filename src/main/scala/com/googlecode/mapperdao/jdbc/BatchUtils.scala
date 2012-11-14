package com.googlecode.mapperdao.jdbc

import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.PreparedStatementCallback
import java.sql.PreparedStatement
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter
import org.springframework.jdbc.support.JdbcUtils

/**
 * @author kostantinos.kougios
 *
 * Nov 14, 2012
 */
class BatchUtils {
	def batchUpdate(jdbc: JdbcOperations, sql: String, pss: BatchPreparedStatementSetter) {
		jdbc.execute(sql, new PreparedStatementCallback[Array[Int]] {
			override def doInPreparedStatement(ps: PreparedStatement) = {
				val batchSize = pss.getBatchSize
				if (JdbcUtils.supportsBatchUpdates(ps.getConnection)) {
					for (i <- 0 until batchSize) {
						pss.setValues(ps, i)
						ps.addBatch()
					}
					val result = ps.executeBatch()
					val generatedKeys = ps.getGeneratedKeys()
					while (generatedKeys.next) {
						val id = generatedKeys.getInt(1)
						println(id)
					}
					generatedKeys.close()
					result
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