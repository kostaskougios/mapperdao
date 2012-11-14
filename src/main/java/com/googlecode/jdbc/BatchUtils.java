package com.googlecode.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * @author kostantinos.kougios
 *
 * 14 Nov 2012
 */
public class BatchUtils
{
	public static int[] batchUpdate(JdbcOperations jdbc, String sql, final BatchPreparedStatementSetter pss) throws DataAccessException
	{

		return jdbc.execute(sql, new PreparedStatementCallback<int[]>()
		{
			public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException
			{
				try
				{
					int batchSize = pss.getBatchSize();
					InterruptibleBatchPreparedStatementSetter ipss = (pss instanceof InterruptibleBatchPreparedStatementSetter ? (InterruptibleBatchPreparedStatementSetter) pss : null);
					if (JdbcUtils.supportsBatchUpdates(ps.getConnection()))
					{
						for (int i = 0; i < batchSize; i++)
						{
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i))
							{
								break;
							}
							ps.addBatch();
						}
						return ps.executeBatch();
					}
					throw new IllegalStateException("batch updates not supported by this jdbc driver");
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
				} finally
				{
					if (pss instanceof ParameterDisposer)
					{
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}

}
