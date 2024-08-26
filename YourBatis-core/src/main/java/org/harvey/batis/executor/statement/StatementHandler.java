package org.harvey.batis.executor.statement;

import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.executor.param.ParameterHandler;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.mapping.BoundSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:33
 */
public interface StatementHandler {

    Statement prepare(Connection connection, Integer transactionTimeout)
            throws SQLException;

    /**
     * 将参数填入{@link Statement}
     */
    void parameterize(Statement statement)
            throws SQLException;

    void batch(Statement statement)
            throws SQLException;

    int update(Statement statement)
            throws SQLException;

    <E> List<E> query(Statement statement, ResultHandler<?> resultHandler)
            throws SQLException;

    <E> Cursor<E> queryCursor(Statement statement)
            throws SQLException;

    BoundSql getBoundSql();

    ParameterHandler getParameterHandler();

}