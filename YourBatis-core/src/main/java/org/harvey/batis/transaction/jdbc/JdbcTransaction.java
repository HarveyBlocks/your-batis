package org.harvey.batis.transaction.jdbc;

import org.harvey.batis.exception.transaction.TransactionException;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;
import org.harvey.batis.transaction.Transaction;
import org.harvey.batis.util.enums.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@inheritDoc}
 * 基于JDBC的事务
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 17:32
 */
public class JdbcTransaction implements Transaction {
    private static final Log LOG = LogFactory.getLog(JdbcTransaction.class);

    protected Connection connection;
    protected DataSource dataSource;
    protected TransactionIsolationLevel level;
    protected boolean autoCommit;

    /**
     * @param ds 从这个Datasource中源源不断地获取Connection
     */
    public JdbcTransaction(DataSource ds, TransactionIsolationLevel desiredLevel, boolean desiredAutoCommit) {
        dataSource = ds;
        level = desiredLevel;
        autoCommit = desiredAutoCommit;
    }

    /**
     * @param connection 就这个Connection和本类这个Transaction强绑定了
     */
    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            this.openConnection();
        }
        return connection;
    }

    /**
     * 从{@link #dataSource}中获取Connection, 然后给connection注入配置({@link #level}和{@link #autoCommit})
     */
    protected void openConnection() throws SQLException {
        LOG.debugIfEnable("Opening JDBC Connection");
        connection = dataSource.getConnection();
        if (level != null) {
            connection.setTransactionIsolation(level.level());
        }
        this.setDesiredAutoCommit(autoCommit);
    }

    /**
     * @param desiredAutoCommit 如果和原来配置的一样,
     *                          就不会调用{@link Connection#setAutoCommit(boolean)}去改配置了
     * @throws TransactionException 由于{@link Connection#setAutoCommit(boolean)}和{@link Connection#getAutoCommit()}
     *                              抛出的SQLException而产生的异常
     */
    protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
        try {
            if (connection.getAutoCommit() == desiredAutoCommit) {
                // 和当前自动提交一样, 不需要该
                return;
            }
            // 不一样, 需要改
            LOG.debugIfEnable("Setting autocommit to " +
                    desiredAutoCommit +
                    " on JDBC Connection [" + connection + "]");
            connection.setAutoCommit(desiredAutoCommit);
        } catch (SQLException e) {
            // Only a very poorly implemented driver would fail here,
            // and there's not much we can do about that.
            throw new TransactionException("Error configuring AutoCommit.  " +
                    "Your driver may not support getAutoCommit() or setAutoCommit(). " +
                    "Requested setting: " + desiredAutoCommit + ".  Cause: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws SQLException {
        if (connection == null) {
            return;
        }
        if (connection.getAutoCommit()) {
            // 已经打开了自动提交, 不需要提交
            return;
        }
        LOG.debugIfEnable("Committing JDBC Connection [" + connection + "]");
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        if (connection == null) {
            return;
        }
        if (connection.getAutoCommit()) {
            return;
        }
        LOG.debugIfEnable("Rolling back JDBC Connection [" + connection + "]");
        connection.rollback();
    }

    /**
     * {@inheritDoc}
     * 关闭连接
     *
     * @see #resetAutoCommit(Connection)
     */
    @Override
    public void close() throws SQLException {
        if (connection == null) {
            return;
        }
        // 将连接转成自动提交
        this.resetAutoCommit(this.connection);
        LOG.debugIfEnable("Closing JDBC Connection [" + connection + "]");
        connection.close();
    }

    /**
     * 如果原来是手动提交的, 转成自动提交
     */
    protected void resetAutoCommit(Connection conn) {
        try {
            if (conn.getAutoCommit()) {
                // 如果已经自动提交了
                return;
            }
            // 如果只执行了selects操作，MyBatis 不会在连接上调用 commit/rollback。
            // 某些数据库使用 select 语句启动事务，并且它们要求在关闭连接之前进行 commit/rollback。
            // 解决方法是在关闭连接之前将自动提交设置为 true。
            LOG.debugIfEnable("Resetting autocommit to true on JDBC Connection [" + conn + "]");
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            // Sybase 在此处抛出异常
            LOG.debugIfEnable("Error resetting autocommit to true "
                    + "before closing the connection.  Cause: " + e);
        }
    }

    /**
     * @return 总是返回null
     */
    @Override
    public Integer getTimeout() {
        return null;
    }
}
