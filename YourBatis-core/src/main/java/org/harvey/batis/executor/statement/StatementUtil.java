package org.harvey.batis.executor.statement;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-20 22:06
 */
public class StatementUtil {
    private StatementUtil() {
        // NOP
    }

    /**
     * 更新查询timeout以应对事务timeout<br>
     * 如果查询timeout为null or 0 or 比事务timeout大<br>
     * 则设置成事务timeout<br>
     * 要么两个timeout都没有, 要么查询timeout要小于事务timeout
     *
     * @param statement          目标语句
     * @param queryTimeout       查询timeout
     * @param transactionTimeout 事务timeout
     * @throws SQLException 如果发生数据库访问错误，则在已关闭的{@link Statement}上调用此方法
     */
    public static void applyTransactionTimeout(
            Statement statement, Integer queryTimeout, Integer transactionTimeout)
            throws SQLException {
        if (transactionTimeout == null) {
            return;
        }
        if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
            statement.setQueryTimeout(transactionTimeout);
        }
    }
}
