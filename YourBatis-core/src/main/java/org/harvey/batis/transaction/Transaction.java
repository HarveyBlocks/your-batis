package org.harvey.batis.transaction;


import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 20:15
 */
public interface Transaction {
    /**
     * 检索数据库本{@link Transaction}对象的数据库连接
     *
     * @return DataBase connection
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交本{@link Transaction}对象的数据库连接
     */
    void commit() throws SQLException;

    /**
     * 回滚本{@link Transaction}对象的数据库连接
     */
    void rollback() throws SQLException;

    /**
     * 关闭本{@link Transaction}对象的数据库连接
     */
    void close() throws SQLException;

    Integer getTimeout();

}
