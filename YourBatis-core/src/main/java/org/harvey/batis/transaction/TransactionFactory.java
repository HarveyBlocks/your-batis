package org.harvey.batis.transaction;

import org.harvey.batis.util.enums.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * TODO
 * 创建 {@link Transaction}实例
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 16:55
 */
public interface TransactionFactory {

    /**
     * @param props transaction factory 的用户配置
     */
    void setProperties(Properties props);

    /**
     * 从现有连接创建一个 {@link Transaction}
     *
     * @param conn 现有数据库连接
     */
    Transaction newTransaction(Connection conn);

    /**
     * Creates a {@link Transaction} out of a datasource.
     *
     * @param dataSource 要从中获取连接的 {@link DataSource}
     * @param level      事务隔离级别
     * @param autoCommit 需要自动提交, 则为 {@code true}
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);


}
