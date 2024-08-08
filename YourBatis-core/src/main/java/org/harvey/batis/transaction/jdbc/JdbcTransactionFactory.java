package org.harvey.batis.transaction.jdbc;

import org.harvey.batis.enums.TransactionIsolationLevel;
import org.harvey.batis.transaction.Transaction;
import org.harvey.batis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 17:34
 */
public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public void setProperties(Properties props) {
        // Do Nothing
    }

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(ds, level, autoCommit);
    }
}
