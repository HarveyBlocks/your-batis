package org.harvey.batis.executor;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.executor.statement.StatementHandler;
import org.harvey.batis.io.log.Log;
import org.harvey.batis.mapping.BoundSql;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-15 00:16
 */
public class SimpleExecutor extends BaseExecutor {
    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(
                    this, ms, parameter, RowBounds.DEFAULT, null, null);
            stmt = this.prepareStatement(handler, ms.getStatementLog());
            return handler.update(stmt);
        } finally {
            this.closeStatement(stmt);
        }
    }

    /**
     * @see StatementHandler#query(Statement, ResultHandler)
     */
    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 获取StatementHandler
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            stmt = this.prepareStatement(handler, ms.getStatementLog());
            return handler.query(stmt, resultHandler);
        } finally {
            super.closeStatement(stmt);
        }
    }

    /**
     * 准备{@link Statement}, 并注入参数
     *
     * @see BaseExecutor#getConnection(Log)
     * @see StatementHandler#prepare(Connection, Integer)
     * @see StatementHandler#parameterize(Statement)
     */
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Connection connection = super.getConnection(statementLog);
        // 获取Statement
        Statement stmt = handler.prepare(connection, transaction.getTimeout());
        // 注入参数
        handler.parameterize(stmt);
        return stmt;
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) {
        return Collections.emptyList();
    }


}
